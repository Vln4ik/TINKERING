from __future__ import annotations

from dataclasses import dataclass
import inspect

from app.core.config import settings


@dataclass
class GigaChatAnswer:
    text: str


class GigaChatClient:
    def __init__(self) -> None:
        self.credentials = settings.gigachat_credentials
        self.scope = settings.gigachat_scope
        self.model = settings.gigachat_model
        self.verify_ssl = settings.gigachat_verify_ssl

    def is_configured(self) -> bool:
        return bool(self.credentials)

    def ask_support(self, user_text: str) -> GigaChatAnswer:
        """
        Uses official `gigachat` python SDK.
        If not configured, returns a safe fallback message.
        """
        if not self.is_configured():
            return GigaChatAnswer(
                text="Техподдержка сейчас недоступна (не настроены креды GigaChat на сервере)."
            )

        try:
            from gigachat import GigaChat
        except Exception:
            return GigaChatAnswer(text="На сервере не установлен пакет `gigachat`.")

        system_prompt = (
            "Ты — служба поддержки приложения для знакомств. "
            "Отвечай кратко, дружелюбно, по делу. "
            "Если не хватает данных — задай уточняющий вопрос."
        )

        prompt = f"{system_prompt}\n\nСообщение пользователя:\n{user_text}"

        try:
            # Detect which init kwarg controls SSL verification in the installed SDK.
            base_kwargs = {"credentials": self.credentials, "scope": self.scope, "model": self.model}
            params = set(inspect.signature(GigaChat.__init__).parameters.keys())
            verify_kw = None
            for cand in ("verify_ssl_certs", "verify_ssl", "verify"):
                if cand in params:
                    verify_kw = cand
                    break
            if verify_kw is not None:
                base_kwargs[verify_kw] = self.verify_ssl
            elif self.verify_ssl is False:
                # SDK can't disable SSL verification; fail fast with clear instruction.
                return GigaChatAnswer(
                    text="GigaChat SDK не поддерживает отключение SSL verify в этой версии. "
                    "Нужно установить доверенный CA (Russian Trusted Sub CA/Root) на сервер/в контейнер."
                )

            giga_ctx = GigaChat(**base_kwargs)

            with giga_ctx as giga:
                # Many SDK versions expect a plain string prompt for chat()
                resp = giga.chat(prompt)
        except Exception as e:
            msg = str(e) or repr(e)
            cause = getattr(e, "__cause__", None)
            if cause:
                msg = f"{msg}; cause={type(cause).__name__}: {cause}"
            hint = ""
            if "certificate" in msg.lower() or "ssl" in msg.lower():
                hint = " (похоже на SSL; проверьте ca-certificates или временно GIGACHAT_VERIFY_SSL=false)"
            return GigaChatAnswer(text=f"Не удалось получить ответ от GigaChat: {type(e).__name__}: {msg}{hint}")

        text = ""
        # SDK returns different shapes depending on version; handle common ones.
        if isinstance(resp, str):
            text = resp
        elif isinstance(resp, dict):
            text = (resp.get("choices") or [{}])[0].get("message", {}).get("content", "") or ""
        else:
            # object-like
            try:
                text = resp.choices[0].message.content  # type: ignore[attr-defined]
            except Exception:
                text = ""

        return GigaChatAnswer(text=text or "Не получилось сформировать ответ. Попробуйте ещё раз.")


