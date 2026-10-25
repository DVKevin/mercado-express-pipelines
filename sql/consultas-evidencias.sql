-- ============================================================
-- Mercado Express - SELECTs de evidência do CRUD (Query editor)
-- Rode a consulta correspondente depois de CADA operação no App
-- ============================================================

-- 1) FORNECEDORES (tb_fornecedor)
SELECT id, nome, cnpj, email, telefone, cidade
FROM tb_fornecedor
ORDER BY id DESC;

-- 2) PRODUTOS (tds_sec_mvc_tb_mercado) com o fornecedor (prova do relacionamento)
SELECT p.id, p.nome, p.categoria, p.preco_custo, p.preco_venda, p.estoque, p.unidade,
       p.fornecedor_id, f.nome AS fornecedor
FROM tds_sec_mvc_tb_mercado p
LEFT JOIN tb_fornecedor f ON f.id = p.fornecedor_id
ORDER BY p.id DESC;

-- 3) Quantidade de produtos por fornecedor (relacionamento 1:N)
SELECT f.id, f.nome, COUNT(p.id) AS qtd_produtos
FROM tb_fornecedor f
LEFT JOIN tds_sec_mvc_tb_mercado p ON p.fornecedor_id = f.id
GROUP BY f.id, f.nome
ORDER BY f.id;

-- 4) Usuários
SELECT id, nome, email, role, criado_em FROM tds_users_mercado ORDER BY id;
