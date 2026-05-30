-- Si le compte admin@erdv.com a été créé par inscription (rôle USER), le repasser en ADMIN.
-- Idempotent : n'affecte que cette ligne si elle existe.
UPDATE utilisateurs SET role = 'ADMIN' WHERE email = 'admin@erdv.com';
