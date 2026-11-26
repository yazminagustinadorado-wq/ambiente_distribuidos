import os
import uuid

from locust import FastHttpUser, LoadTestShape, between, task


class TP8User(FastHttpUser):
    host = "http://127.0.0.1:8001"
    wait_time = between(0.1, 0.5)

    @task(6)
    def listar_eventos(self):
        with self.client.get("/eventos", name="GET /eventos", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")

    @task(3)
    def crear_evento(self):
        payload = {
            "clave": f"locust-{uuid.uuid4().hex}",
            "valor": "carga-locust",
        }
        with self.client.post(
            "/eventos",
            json=payload,
            name="POST /eventos",
            catch_response=True,
        ) as response:
            if response.status_code == 201:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}: {response.text[:120]}")

    @task(1)
    def health(self):
        with self.client.get("/health", name="GET /health", catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")


if os.getenv("TP10_SHAPE") == "stress":

    class StressShape(LoadTestShape):
        stages = [
            (30, 50, 5),
            (30, 75, 5),
            (30, 100, 5),
            (30, 125, 5),
            (30, 150, 5),
            (30, 175, 5),
            (30, 200, 5),
            (30, 225, 5),
            (60, 250, 5),
        ]

        def tick(self):
            elapsed = self.get_run_time()
            total = 0
            for duration, users, spawn_rate in self.stages:
                if elapsed < total + duration:
                    return users, spawn_rate
                total += duration
            return None
