-- Aligner jour_semaine sur le type Integer attendu par Hibernate (ddl-auto=validate)
ALTER TABLE plages_recurrentes
    ALTER COLUMN jour_semaine TYPE INTEGER;
