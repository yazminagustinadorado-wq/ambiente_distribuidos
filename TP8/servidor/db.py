import os

from psycopg import connect
from psycopg.rows import dict_row

WRITE_URL = os.getenv("POSTGRES_WRITE_URL", "postgresql://tp8:tp8@localhost:5432/tp8")
READ_URL = os.getenv("POSTGRES_READ_URL", "postgresql://tp8:tp8@localhost:5433/tp8")
PROMOTED_URL = os.getenv("POSTGRES_PROMOTED_URL", "postgresql://tp8:tp8@localhost:5433/tp8")

state = {"failover": False}


def write_url():
    return PROMOTED_URL if state["failover"] else WRITE_URL


def read_url():
    return PROMOTED_URL if state["failover"] else READ_URL


def fetchall(url, sql, params=None):
    with connect(url, row_factory=dict_row, connect_timeout=3) as conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            return cur.fetchall()


def fetchone(url, sql, params=None):
    with connect(url, row_factory=dict_row, connect_timeout=3) as conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            return cur.fetchone()


def execute(url, sql, params=None):
    with connect(url, row_factory=dict_row, connect_timeout=3) as conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            return cur.fetchone() if cur.description else None
