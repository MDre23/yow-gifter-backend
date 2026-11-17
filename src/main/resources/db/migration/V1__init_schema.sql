-- =============================================================================
-- Migration V1 : Schéma Initial Yow Gifter (ENSPY / UY1 4GI - UE Supervision)
-- Stack : Spring Boot 3 · PostgreSQL 16+ · Flyway
-- =============================================================================

-- 1. Table Utilisateurs (Users)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    city VARCHAR(100) NOT NULL DEFAULT 'Yaoundé',
    neighborhood VARCHAR(150) NOT NULL,
    age INTEGER CHECK (age >= 16 AND age <= 120),
    bio TEXT,
    avatar_url VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- 2. Table Catégories (Categories)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(100)
);

CREATE INDEX idx_categories_slug ON categories(slug);

-- 3. Table Annonces de Dons (Donations)
CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    donor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    condition VARCHAR(50) NOT NULL CHECK (condition IN ('NEUF', 'COMME_NEUF', 'TRES_BON', 'BON', 'POUR_PIECES')),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'GIVEN', 'SUSPENDED')),
    city VARCHAR(100) NOT NULL DEFAULT 'Yaoundé',
    neighborhood VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donations_donor ON donations(donor_id);
CREATE INDEX idx_donations_category ON donations(category_id);
CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donations_created_at ON donations(created_at DESC);

-- 4. Table Photos des Dons (Donation Images - Limite stricte de 2 photos contrôlée au niveau métier)
CREATE TABLE donation_images (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL REFERENCES donations(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donation_images_donation ON donation_images(donation_id);

-- 5. Table Demandes / File d'Attente FIFO (Requests)
CREATE TABLE requests (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL REFERENCES donations(id) ON DELETE CASCADE,
    beneficiary_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'PRIORITY', 'CANCELLED', 'EXPIRED', 'COMPLETED')),
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_donation_beneficiary UNIQUE (donation_id, beneficiary_id)
);

-- Index pour garantir un tri et une promotion FIFO sans latence
CREATE INDEX idx_requests_fifo ON requests(donation_id, requested_at ASC);
CREATE INDEX idx_requests_beneficiary ON requests(beneficiary_id);
CREATE INDEX idx_requests_status ON requests(status);

-- 6. Table Conversations Privées (Conversations - Uniquement Donateur et Bénéficiaire Prioritaire)
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL REFERENCES donations(id) ON DELETE CASCADE,
    donor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    beneficiary_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_conversation_donation_beneficiary UNIQUE (donation_id, beneficiary_id)
);

CREATE INDEX idx_conversations_donation ON conversations(donation_id);
CREATE INDEX idx_conversations_donor ON conversations(donor_id);
CREATE INDEX idx_conversations_beneficiary ON conversations(beneficiary_id);

-- 7. Table Messages Bilatéraux (Messages)
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    content TEXT NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id, sent_at ASC);
CREATE INDEX idx_messages_sender ON messages(sender_id);
