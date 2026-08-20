-- Clean existing records if any
DELETE FROM tbl_expense;
DELETE FROM tbl_income;
DELETE FROM tbl_categories;
DELETE FROM tbl_profile;

-- ==============================================================================
-- 1. SEED PROFILES (5 Users)
-- Password for all users is 'Password@123' ($2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.)
-- ==============================================================================
INSERT INTO tbl_profile (id, full_name, email, password, profile_picture_url, is_active, activation_token, created_at, updated_at)
VALUES 
(1, 'Test User', 'testuser@example.com', '$2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.', 'http://example.com/avatar1.jpg', true, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Inactive User', 'inactive@example.com', '$2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.', NULL, false, 'sample-activation-token-12345', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Alex Johnson', 'alex.johnson@example.com', '$2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.', 'http://example.com/avatar3.jpg', true, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Sarah Williams', 'sarah.w@example.com', '$2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.', 'http://example.com/avatar4.jpg', true, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Michael Brown', 'michael.b@example.com', '$2a$10$wT8m967c.7lZlA8P2gL.eu7Xo77pC2fF9Xj8lV96X9e3s5x9gZ8d.', 'http://example.com/avatar5.jpg', true, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==============================================================================
-- 2. SEED CATEGORIES (30 Categories)
-- All category names are unique across the table
-- ==============================================================================
INSERT INTO tbl_categories (id, name, type, icon, profile_id, created_at, updated_at)
VALUES 
-- Income Categories (1-15)
(1, 'Salary', 'income', 'wallet', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Freelance', 'income', 'laptop', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Stock Dividends', 'income', 'trending-up', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Rental Income', 'income', 'home', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Consulting', 'income', 'briefcase', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Interest Income', 'income', 'percent', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Crypto Gains', 'income', 'dollar-sign', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Affiliate Marketing', 'income', 'share-2', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'YouTube Revenue', 'income', 'video', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Book Royalties', 'income', 'book', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'E-commerce Sales', 'income', 'shopping-bag', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Tutoring', 'income', 'award', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Real Estate Flipping', 'income', 'building', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Cashback Rewards', 'income', 'gift', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Yearly Bonus', 'income', 'star', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Expense Categories (16-30)
(16, 'Groceries', 'expense', 'shopping-cart', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Utilities', 'expense', 'bolt', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Housing & Rent', 'expense', 'home', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Dining Out', 'expense', 'coffee', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Transportation', 'expense', 'truck', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Entertainment', 'expense', 'film', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Healthcare & Medical', 'expense', 'activity', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Education & Courses', 'expense', 'book-open', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Gym & Fitness', 'expense', 'heart', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Travel & Vacations', 'expense', 'compass', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Insurance', 'expense', 'shield', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'Clothing & Apparel', 'expense', 'tag', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'Gadgets & Tech', 'expense', 'cpu', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'Pet Care', 'expense', 'smile', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'Charity & Donations', 'expense', 'heart', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==============================================================================
-- 3. SEED INCOMES (30 Records)
-- All income names are unique
-- ==============================================================================
INSERT INTO tbl_income (id, name, icon, date, amount, category_id, profile_id, creation_at, updated_at)
VALUES 
(1, 'Monthly Salary', 'wallet', CURRENT_DATE, 5000.00, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Client Project', 'laptop', CURRENT_DATE, 1500.00, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Apple Stock Dividend', 'trending-up', CURRENT_DATE, 180.50, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Apartment 4B Rent', 'home', CURRENT_DATE, 1200.00, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Architecture Advisory', 'briefcase', CURRENT_DATE, 850.00, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Savings Account Interest', 'percent', CURRENT_DATE, 45.20, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Bitcoin Staking Reward', 'dollar-sign', CURRENT_DATE, 320.00, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Amazon Affiliate Payout', 'share-2', CURRENT_DATE, 210.75, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Google AdSense Revenue', 'video', CURRENT_DATE, 640.00, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Kindle Book Sales', 'book', CURRENT_DATE, 150.00, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Etsy Handmade Crafts', 'shopping-bag', CURRENT_DATE, 430.00, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Private Math Tutoring', 'award', CURRENT_DATE, 300.00, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Suburban Plot Sale Gain', 'building', CURRENT_DATE, 4500.00, 13, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Credit Card Cashback', 'gift', CURRENT_DATE, 65.40, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Annual Performance Bonus', 'star', CURRENT_DATE, 3500.00, 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Overtime Pay August', 'wallet', CURRENT_DATE, 450.00, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Website Redesign Gig', 'laptop', CURRENT_DATE, 950.00, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Index Fund Distribution', 'trending-up', CURRENT_DATE, 275.00, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Commercial Unit Lease', 'home', CURRENT_DATE, 2100.00, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Security Audit Consultation', 'briefcase', CURRENT_DATE, 1250.00, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Fixed Deposit Maturity', 'percent', CURRENT_DATE, 520.00, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Ethereum Trading Profit', 'dollar-sign', CURRENT_DATE, 780.00, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Sponsorship Deal', 'video', CURRENT_DATE, 1100.00, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Technical Writing Gig', 'book', CURRENT_DATE, 380.00, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Shopify Store Revenue', 'shopping-bag', CURRENT_DATE, 890.00, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Physics Online Coaching', 'award', CURRENT_DATE, 250.00, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'Bank Referral Bonus', 'gift', CURRENT_DATE, 100.00, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'Quarterly Target Incentive', 'star', CURRENT_DATE, 1500.00, 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'UI/UX Mobile Design Project', 'laptop', CURRENT_DATE, 1350.00, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'Blogging Monetization', 'share-2', CURRENT_DATE, 290.00, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==============================================================================
-- 4. SEED EXPENSES (30 Records)
-- All expense names are unique
-- ==============================================================================
INSERT INTO tbl_expense (id, name, icon, date, amount, category_id, profile_id, creation_at, updated_at)
VALUES 
(1, 'Supermarket Shopping', 'shopping-cart', CURRENT_DATE, 250.00, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Electricity Bill', 'bolt', CURRENT_DATE, 120.00, 17, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Monthly Apartment Rent', 'home', CURRENT_DATE, 1400.00, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Fine Dining Restaurant', 'coffee', CURRENT_DATE, 85.50, 19, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Monthly Metro Pass', 'truck', CURRENT_DATE, 60.00, 20, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Movie Night Tickets', 'film', CURRENT_DATE, 35.00, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Dental Checkup', 'activity', CURRENT_DATE, 110.00, 22, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Udemy Spring Boot Course', 'book-open', CURRENT_DATE, 29.99, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Annual Gym Membership', 'heart', CURRENT_DATE, 450.00, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Weekend Flight Tickets', 'compass', CURRENT_DATE, 380.00, 25, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Health Insurance Premium', 'shield', CURRENT_DATE, 220.00, 26, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Winter Jacket Shopping', 'tag', CURRENT_DATE, 145.00, 27, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Noise Cancelling Headphones', 'cpu', CURRENT_DATE, 299.00, 28, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Veterinary Clinic Visit', 'smile', CURRENT_DATE, 75.00, 29, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Red Cross Donation', 'heart', CURRENT_DATE, 50.00, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Organic Grocery Delivery', 'shopping-cart', CURRENT_DATE, 135.00, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'High Speed Internet Bill', 'bolt', CURRENT_DATE, 70.00, 17, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Society Maintenance Fee', 'home', CURRENT_DATE, 95.00, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Coffee Shop Meetings', 'coffee', CURRENT_DATE, 42.00, 19, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Uber Rides for Week', 'truck', CURRENT_DATE, 55.00, 20, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Netflix & Spotify Subs', 'film', CURRENT_DATE, 24.99, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Pharmacy Prescriptions', 'activity', CURRENT_DATE, 48.00, 22, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'AWS Certification Exam', 'book-open', CURRENT_DATE, 150.00, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Whey Protein Powder', 'heart', CURRENT_DATE, 65.00, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Hotel Booking Goa', 'compass', CURRENT_DATE, 520.00, 25, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Car Insurance Renewal', 'shield', CURRENT_DATE, 310.00, 26, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'Running Shoes Purchase', 'tag', CURRENT_DATE, 120.00, 27, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'Mechanical Keyboard', 'cpu', CURRENT_DATE, 110.00, 28, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'Premium Dog Food', 'smile', CURRENT_DATE, 58.00, 29, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'Local Animal Shelter Support', 'heart', CURRENT_DATE, 40.00, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==============================================================================
-- 5. RESTART SEQUENCES
-- Prevent key collisions on auto-generated runtime insertions during tests
-- ==============================================================================
ALTER TABLE tbl_profile ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE tbl_categories ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE tbl_income ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE tbl_expense ALTER COLUMN id RESTART WITH 1000;
