from __future__ import annotations

import uuid

import httpx


class RecoClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")

    async def rank_candidates(self, user_id: uuid.UUID, candidate_ids: list[uuid.UUID]) -> list[uuid.UUID]:
        """
        Contract (expected from your ML service):
        POST {base_url}/rank
        body: { "user_id": "...", "candidate_ids": ["...","..."] }
        resp: { "ranked_candidate_ids": ["...","..."] }
        """
        async with httpx.AsyncClient(timeout=10) as client:
            r = await client.post(
                f"{self.base_url}/rank",
                json={"user_id": str(user_id), "candidate_ids": [str(x) for x in candidate_ids]},
            )
            r.raise_for_status()
            data = r.json()
            ranked = [uuid.UUID(x) for x in data.get("ranked_candidate_ids", [])]
            if not ranked:
                return candidate_ids
            # Keep only known ids + append leftovers
            ranked_set = set(ranked)
            leftovers = [x for x in candidate_ids if x not in ranked_set]
            return ranked + leftovers


