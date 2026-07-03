-- =============================================
-- Dados iniciais de exemplo para o Cuponomia
-- Executado automaticamente ao iniciar o projeto
-- =============================================

-- Cupom de desconto fixo: R$ 25,00 off
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'BEMVINDO25', 'Cupom de boas-vindas: R$ 25 de desconto', 'FIXED', 25.00, true,
        50.00, '2026-12-31T23:59:59', true, NULL,
        '2026-01-01T00:00:00', '2026-01-01T00:00:00', 0);

-- Cupom de desconto percentual: 15% off
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'VERAO15', '15% de desconto em compras de verão', 'PERCENTAGE', 15.00, true,
        100.00, '2026-12-31T23:59:59', true, 1000,
        '2026-01-15T00:00:00', '2026-01-15T00:00:00', 0);

-- Cupom de desconto fixo: R$ 10,00 off (sem regras extras)
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'PROMO10', 'Desconto rápido de R$ 10', 'FIXED', 10.00, true,
        NULL, NULL, false, NULL,
        '2026-02-01T00:00:00', '2026-02-01T00:00:00', 0);

-- Cupom percentual: 50% off (cupom especial, uso único, limite de 50 usos)
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('d4e5f6a7-b8c9-0123-defa-234567890123', 'MEGA50', 'Mega desconto: 50% off no pedido', 'PERCENTAGE', 50.00, true,
        200.00, '2026-06-30T23:59:59', true, 50,
        '2026-03-01T00:00:00', '2026-03-01T00:00:00', 0);

-- Cupom expirado (para demonstrar validação de expiração)
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('e5f6a7b8-c9d0-1234-efab-345678901234', 'EXPIRADO20', 'Cupom expirado para teste', 'FIXED', 20.00, true,
        NULL, '2025-12-31T23:59:59', false, NULL,
        '2025-01-01T00:00:00', '2025-01-01T00:00:00', 0);

-- Cupom desativado (para demonstrar validação de status)
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('f6a7b8c9-d0e1-2345-fabc-456789012345', 'INATIVO30', 'Cupom desativado para teste', 'PERCENTAGE', 30.00, false,
        NULL, '2026-12-31T23:59:59', false, NULL,
        '2026-01-01T00:00:00', '2026-04-01T00:00:00', 0);

-- =============================================
-- 🎯 Easter Egg: Cupom especial para o empregador!
-- Use MAX50 e tenha 50% de desconto na sua contratação 😎
-- =============================================

-- Cupom MAX50: 50% de desconto
INSERT INTO coupons (id, code, description, discount_type, discount_value, active,
                     minimum_order_value, expires_at, single_use_per_client, max_usages,
                     created_at, updated_at, version)
VALUES ('00000000-0a01-0000-0000-000000000100', 'MAX50', '🚀 Use o cupom MAX50 e tenha 50% de desconto na sua contratação.', 'PERCENTAGE', 50.00, true,
        16000.00, '2027-12-31T23:59:59', true, 10,
        '2026-04-29T00:00:00', '2026-04-29T00:00:00', 0);
