CREATE TABLE IF NOT EXISTS eventos (
    id BIGSERIAL PRIMARY KEY,
    clave TEXT NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO eventos (clave, valor) VALUES
    ('demo-inicial-1', 'Registro semilla del primario'),
    ('demo-inicial-2', 'Segundo registro semilla')
ON CONFLICT (clave) DO NOTHING;
