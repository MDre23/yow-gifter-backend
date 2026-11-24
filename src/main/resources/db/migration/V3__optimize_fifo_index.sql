-- Migration V3 : Optimisation de l'index pour la concurrence FIFO
DROP INDEX IF EXISTS idx_requests_fifo;

CREATE INDEX idx_requests_fifo_active
ON requests (donation_id, requested_at ASC)
WHERE status IN ('WAITING', 'PRIORITY');