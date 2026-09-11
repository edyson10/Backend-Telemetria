#!/usr/bin/env python3
"""
Simulador de telemetría para la prueba técnica de Fleet Telemetry.

Requisitos cubiertos:
- 5 vehículos por defecto.
- Intervalo aleatorio de 2 a 5 segundos.
- Aproximadamente 10% de mensajes duplicados.
- Aproximadamente 5% de mensajes inválidos.
- Permite cambiar la URL del backend con --base-url.

Ejemplos:
    python simulator.py --base-url http://localhost:8080
    python simulator.py --base-url http://ec2-public-dns:8080

Requiere:
    pip install requests
"""

import argparse
import copy
import random
import time
from datetime import datetime, timezone

import requests


DEFAULT_VEHICLES = [
    "VH-SIM-001",
    "VH-SIM-002",
    "VH-SIM-003",
    "VH-SIM-004",
    "VH-SIM-005",
]

# Coordenadas iniciales cercanas entre sí.
INITIAL_POSITIONS = {
    "VH-SIM-001": [6.24420, -75.58120],
    "VH-SIM-002": [6.24510, -75.58040],
    "VH-SIM-003": [6.24360, -75.58210],
    "VH-SIM-004": [6.24600, -75.57980],
    "VH-SIM-005": [6.24290, -75.58090],
}


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--base-url",
        default="http://localhost:8080",
        help="URL base del backend",
    )
    parser.add_argument(
        "--min-interval",
        type=float,
        default=2.0,
        help="Intervalo mínimo entre ciclos",
    )
    parser.add_argument(
        "--max-interval",
        type=float,
        default=5.0,
        help="Intervalo máximo entre ciclos",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=10.0,
        help="Timeout HTTP",
    )
    return parser.parse_args()


def now_iso():
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")


def move_position(position):
    latitude, longitude = position

    # Movimiento pequeño para simular GPS.
    latitude += random.uniform(-0.00025, 0.00025)
    longitude += random.uniform(-0.00025, 0.00025)

    return [latitude, longitude]


def create_valid_payload(vehicle_id, position):
    return {
        "vehicleId": vehicle_id,
        "lat": round(position[0], 6),
        "lng": round(position[1], 6),
        "timestamp": now_iso(),
    }


def create_invalid_payload(vehicle_id):
    """
    Payload intencionalmente incorrecto.
    La API debe rechazarlo con un 4xx.
    """
    return {
        "vehicleId": vehicle_id,
        "lat": "NOT_A_NUMBER",
        "lng": None,
        "timestamp": "NOT_A_TIMESTAMP",
    }


def send(session, url, payload, timeout):
    try:
        response = session.post(
            url,
            json=payload,
            timeout=timeout,
        )

        if 200 <= response.status_code < 300:
            return f"{response.status_code} ACCEPTED"

        if 400 <= response.status_code < 500:
            return f"{response.status_code} INVALID/DUPLICATE"

        return f"{response.status_code} ERROR"

    except requests.RequestException as exc:
        return f"NETWORK_ERROR: {exc}"


def main():
    args = parse_args()

    if args.min_interval <= 0 or args.max_interval < args.min_interval:
        raise SystemExit("Intervalo inválido.")

    endpoint = (
        args.base_url.rstrip("/")
        + "/api/v1/telemetry"
    )

    positions = copy.deepcopy(INITIAL_POSITIONS)
    last_payload = {}

    counters = {
        "total": 0,
        "normal": 0,
        "duplicate": 0,
        "invalid": 0,
    }

    print("=" * 70)
    print("Fleet Telemetry Simulator")
    print("=" * 70)
    print(f"Endpoint: {endpoint}")
    print(f"Vehicles: {len(DEFAULT_VEHICLES)}")
    print(
        f"Interval: {args.min_interval}-{args.max_interval} seconds"
    )
    print("Duplicate rate: approximately 10%")
    print("Invalid rate: approximately 5%")
    print("Press Ctrl+C to stop.")
    print("=" * 70)

    session = requests.Session()

    try:
        while True:
            for vehicle_id in DEFAULT_VEHICLES:
                # Elegimos el tipo de evento.
                chaos = random.random()

                if chaos < 0.05:
                    # 5% inválidos.
                    payload = create_invalid_payload(vehicle_id)
                    event_type = "INVALID"
                    counters["invalid"] += 1

                elif chaos < 0.15 and vehicle_id in last_payload:
                    # Siguiente 10%: repetir exactamente el payload anterior.
                    # Al copiarlo completo, incluido timestamp, se prueba
                    # realmente la deduplicación.
                    payload = copy.deepcopy(last_payload[vehicle_id])
                    event_type = "DUPLICATE"
                    counters["duplicate"] += 1

                else:
                    positions[vehicle_id] = move_position(
                        positions[vehicle_id]
                    )
                    payload = create_valid_payload(
                        vehicle_id,
                        positions[vehicle_id],
                    )
                    last_payload[vehicle_id] = copy.deepcopy(payload)
                    event_type = "NORMAL"
                    counters["normal"] += 1

                counters["total"] += 1

                result = send(
                    session,
                    endpoint,
                    payload,
                    args.timeout,
                )

                print(
                    f"{vehicle_id} | "
                    f"{event_type:<9} | "
                    f"{result} | "
                    f"total={counters['total']} "
                    f"normal={counters['normal']} "
                    f"dup={counters['duplicate']} "
                    f"invalid={counters['invalid']}"
                )

            time.sleep(
                random.uniform(
                    args.min_interval,
                    args.max_interval,
                )
            )

    except KeyboardInterrupt:
        print("\nSimulator stopped.")


if __name__ == "__main__":
    main()
