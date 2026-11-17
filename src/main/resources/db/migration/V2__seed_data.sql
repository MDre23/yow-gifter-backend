-- =============================================================================
-- Migration V2 : Données Initiales Yow Gifter (Catégories officielles & Utilisateurs tests)
-- Mot de passe pour tous les utilisateurs tests : password123 (BCrypt hash)
-- =============================================================================

-- 1. Insertion des 6 Catégories Officielles
INSERT INTO categories (id, slug, name, description, icon_name) VALUES
(1, 'livres-manuels', 'Livres & Manuels', 'Manuels scolaires, polycopies universitaires, annales et romans d''étude.', 'book-open-solid'),
(2, 'vetements-textiles', 'Vêtements & Textiles', 'Vêtements, pulls, vestes, chaussures et linge de maison.', 'tshirt-solid'),
(3, 'mobilier-logement', 'Mobilier & Logement', 'Bureaux d''étude, chaises, étagères, lampes et ustensiles pour étudiants.', 'couch-solid'),
(4, 'informatique-tech', 'Informatique & Tech', 'PC portables, calculatrices scientifiques, écrans, claviers et accessoires.', 'laptop-solid'),
(5, 'equipements-domestiques', 'Équipements domestiques', 'Petit électroménager, vaisselle, bouilloires et ventilateurs.', 'blender-solid'),
(6, 'autres', 'Autres', 'Tout objet utile et réutilisable du quotidien (0 FCFA).', 'box-open-solid')
ON CONFLICT (slug) DO NOTHING;

-- Réajustement de la séquence categories
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- 2. Insertion des Utilisateurs Démonstrateurs
-- Le hash ci-dessous correspond au mot de passe : password123
INSERT INTO users (id, email, password_hash, username, first_name, last_name, phone, city, neighborhood, age, bio, avatar_url, is_verified) VALUES
(1, 'alex.kamga@polytechnique.cm', '$2a$10$eACCYoNOHEqgkVE8aGLOn./r3hM2s9U.sHw65/5Lw6iYc1c6hN83K', 'alex_k', 'Alex', 'Kamga', '+237699112233', 'Yaoundé', 'Melen / Campus UY1', 24, 'Étudiant à l''université, passionné d''informatique et engagé dans l''éco-responsabilité et le don d''objets du quotidien.', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80', TRUE),
(2, 'marie.ngono@univ-yaounde1.cm', '$2a$10$eACCYoNOHEqgkVE8aGLOn./r3hM2s9U.sHw65/5Lw6iYc1c6hN83K', 'marie_n', 'Marie', 'Ngono', '+237677445566', 'Yaoundé', 'ENSPY / Ngoa-Ekellé', 22, 'Étudiante en 4GI, engagée dans la transmission solidaire de manuels et matériel d''études.', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80', TRUE),
(3, 'paul.eboa@gmail.com', '$2a$10$eACCYoNOHEqgkVE8aGLOn./r3hM2s9U.sHw65/5Lw6iYc1c6hN83K', 'paul_e', 'Paul', 'Eboa', '+237655889900', 'Yaoundé', 'Biyem-Assi', 28, 'Citoyen engagé pour le don solidaire et la réduction du gaspillage à Yaoundé.', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80', FALSE)
ON CONFLICT (email) DO NOTHING;

-- Réajustement de la séquence users
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 3. Insertion de Dons Exemples (Contrôle strict max 2 photos par don)
INSERT INTO donations (id, donor_id, category_id, title, description, condition, status, city, neighborhood, quantity) VALUES
(1, 1, 4, 'Ordinateur portable Dell Latitude', 'Core i5, 8GB RAM, SSD 256GB. Batterie tenant 2 heures. Idéal pour bureautique, études universitaires et dev.', 'COMME_NEUF', 'AVAILABLE', 'Yaoundé', 'Melen / Campus UY1', 1),
(2, 2, 1, 'Lot manuels Algorithmique & Réseaux', 'Manuels complets avec annales d''examens corrigées et fiches d''exercices niveau Licence et Master 1.', 'TRES_BON', 'AVAILABLE', 'Yaoundé', 'ENSPY / Ngoa-Ekellé', 1),
(3, 3, 3, 'Bureau d''étude en bois compact', 'Dimensions 90x50 cm avec tiroir fonctionnel. Prêt pour retrait direct sur Yaoundé.', 'BON', 'AVAILABLE', 'Yaoundé', 'Biyem-Assi', 1),
(4, 2, 2, 'Veste d''hiver et pulls chauds', 'Taille M/L, lavés et impeccables. Parfaits pour les soirées fraîches ou la saison des pluies.', 'TRES_BON', 'AVAILABLE', 'Yaoundé', 'Bastos', 2)
ON CONFLICT (id) DO NOTHING;

-- Réajustement de la séquence donations
SELECT setval('donations_id_seq', (SELECT MAX(id) FROM donations));

-- 4. Insertion des Photos de Dons (Exactement 1 ou 2 photos par don)
INSERT INTO donation_images (donation_id, image_url, is_primary) VALUES
(1, 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80', TRUE),
(1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80', FALSE),
(2, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80', TRUE),
(3, 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=800&q=80', TRUE),
(4, 'https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80', TRUE);
