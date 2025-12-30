-- Insert sample person records: famous European left-wing activists
-- Gender: 1=Female, 2=Male, 3=Unspecified
-- Title: 1=DR, 2=MR, 3=MRS, 4=MS, 5=PROF, 6=REV

INSERT INTO person (first_name, last_name, email, phone, date_of_birth, title_id, gender_id, created_by, updated_by) VALUES
    ('Karl', 'Marx', 'karl.marx@example.com', '+49-30-1818', '1818-05-05', 2, 2, 'system', 'system'),
    ('Friedrich', 'Engels', 'friedrich.engels@example.com', '+49-202-1820', '1820-11-28', 2, 2, 'system', 'system'),
    ('Rosa', 'Luxemburg', 'rosa.luxemburg@example.com', '+49-30-1871', '1871-03-05', 4, 1, 'system', 'system'),
    ('Clara', 'Zetkin', 'clara.zetkin@example.com', '+49-711-1857', '1857-07-05', 4, 1, 'system', 'system'),
    ('Antonio', 'Gramsci', 'antonio.gramsci@example.com', '+39-06-1891', '1891-01-22', 2, 2, 'system', 'system'),
    ('Jean-Paul', 'Sartre', 'jean-paul.sartre@example.com', '+33-1-1905', '1905-06-21', 2, 2, 'system', 'system'),
    ('Simone', 'de Beauvoir', 'simone.debeauvoir@example.com', '+33-1-1908', '1908-01-09', 4, 1, 'system', 'system'),
    ('Tony', 'Benn', 'tony.benn@example.com', '+44-20-1925', '1925-04-03', 2, 2, 'system', 'system'),
    ('Enrico', 'Berlinguer', 'enrico.berlinguer@example.com', '+39-06-1922', '1922-05-25', 2, 2, 'system', 'system'),
    ('Olof', 'Palme', 'olof.palme@example.com', '+46-8-1927', '1927-01-30', 2, 2, 'system', 'system'),
    ('Yanis', 'Varoufakis', 'yanis.varoufakis@example.com', '+30-21-1961', '1961-03-24', 1, 2, 'system', 'system'),
    ('Pablo', 'Iglesias', 'pablo.iglesias@example.com', '+34-91-1978', '1978-10-17', 1, 2, 'system', 'system')
;
