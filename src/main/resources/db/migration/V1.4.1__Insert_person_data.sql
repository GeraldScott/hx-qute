-- Insert sample person records: famous European left-wing activists
-- Gender: 1=Female, 2=Male, 3=Unspecified
-- Title: 1=DR, 2=MR, 3=MRS, 4=MS, 5=PROF, 6=REV

INSERT INTO person (first_name, last_name, email, phone, date_of_birth, title_id, gender_id, created_by, updated_by) VALUES
    -- Original records (1-12)
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
    ('Pablo', 'Iglesias', 'pablo.iglesias@example.com', '+34-91-1978', '1978-10-17', 1, 2, 'system', 'system'),

    -- French socialists (13-18)
    ('Jean', 'Jaures', 'jean.jaures@example.com', '+33-1-1859', '1859-09-03', 2, 2, 'system', 'system'),
    ('Leon', 'Blum', 'leon.blum@example.com', '+33-1-1872', '1872-04-09', 2, 2, 'system', 'system'),
    ('Francois', 'Mitterrand', 'francois.mitterrand@example.com', '+33-1-1916', '1916-10-26', 2, 2, 'system', 'system'),
    ('Charles', 'Fourier', 'charles.fourier@example.com', '+33-25-1772', '1772-04-07', 2, 2, 'system', 'system'),
    ('Paul', 'Lafargue', 'paul.lafargue@example.com', '+33-1-1842', '1842-01-15', 1, 2, 'system', 'system'),
    ('Jules', 'Guesde', 'jules.guesde@example.com', '+33-1-1845', '1845-11-11', 2, 2, 'system', 'system'),

    -- German socialists and theorists (19-27)
    ('August', 'Bebel', 'august.bebel@example.com', '+49-30-1840', '1840-02-22', 2, 2, 'system', 'system'),
    ('Wilhelm', 'Liebknecht', 'wilhelm.liebknecht@example.com', '+49-30-1826', '1826-03-29', 2, 2, 'system', 'system'),
    ('Karl', 'Liebknecht', 'karl.liebknecht@example.com', '+49-30-1871', '1871-08-13', 1, 2, 'system', 'system'),
    ('Willy', 'Brandt', 'willy.brandt@example.com', '+49-30-1913', '1913-12-18', 2, 2, 'system', 'system'),
    ('Helmut', 'Schmidt', 'helmut.schmidt@example.com', '+49-40-1918', '1918-12-23', 2, 2, 'system', 'system'),
    ('Ferdinand', 'Lassalle', 'ferdinand.lassalle@example.com', '+49-30-1825', '1825-04-11', 2, 2, 'system', 'system'),
    ('Karl', 'Kautsky', 'karl.kautsky@example.com', '+49-30-1854', '1854-10-16', 1, 2, 'system', 'system'),
    ('Eduard', 'Bernstein', 'eduard.bernstein@example.com', '+49-30-1850', '1850-01-06', 2, 2, 'system', 'system'),
    ('Herbert', 'Marcuse', 'herbert.marcuse@example.com', '+49-30-1898', '1898-07-19', 5, 2, 'system', 'system'),

    -- Italian communists and socialists (28-29)
    ('Palmiro', 'Togliatti', 'palmiro.togliatti@example.com', '+39-06-1893', '1893-03-26', 2, 2, 'system', 'system'),
    ('Pietro', 'Nenni', 'pietro.nenni@example.com', '+39-06-1891', '1891-02-09', 2, 2, 'system', 'system'),

    -- British Labour and socialist figures (30-40)
    ('Keir', 'Hardie', 'keir.hardie@example.com', '+44-20-1856', '1856-08-15', 2, 2, 'system', 'system'),
    ('Clement', 'Attlee', 'clement.attlee@example.com', '+44-20-1883', '1883-01-03', 2, 2, 'system', 'system'),
    ('Aneurin', 'Bevan', 'aneurin.bevan@example.com', '+44-29-1897', '1897-11-15', 2, 2, 'system', 'system'),
    ('Michael', 'Foot', 'michael.foot@example.com', '+44-20-1913', '1913-07-23', 2, 2, 'system', 'system'),
    ('Harold', 'Wilson', 'harold.wilson@example.com', '+44-20-1916', '1916-03-11', 2, 2, 'system', 'system'),
    ('Neil', 'Kinnock', 'neil.kinnock@example.com', '+44-29-1942', '1942-03-28', 2, 2, 'system', 'system'),
    ('Robert', 'Owen', 'robert.owen@example.com', '+44-1686-1771', '1771-05-14', 2, 2, 'system', 'system'),
    ('Eric', 'Hobsbawm', 'eric.hobsbawm@example.com', '+44-20-1917', '1917-06-09', 5, 2, 'system', 'system'),
    ('Edward', 'Thompson', 'edward.thompson@example.com', '+44-20-1924', '1924-02-03', 5, 2, 'system', 'system'),
    ('Sylvia', 'Pankhurst', 'sylvia.pankhurst@example.com', '+44-20-1882', '1882-05-05', 4, 1, 'system', 'system'),
    ('Eleanor', 'Marx', 'eleanor.marx@example.com', '+44-20-1855', '1855-01-16', 4, 1, 'system', 'system'),

    -- Scandinavian social democrats (41-43)
    ('Per Albin', 'Hansson', 'peralbin.hansson@example.com', '+46-8-1885', '1885-10-28', 2, 2, 'system', 'system'),
    ('Tage', 'Erlander', 'tage.erlander@example.com', '+46-8-1901', '1901-06-13', 2, 2, 'system', 'system'),
    ('Gro Harlem', 'Brundtland', 'gro.brundtland@example.com', '+47-22-1939', '1939-04-20', 1, 1, 'system', 'system'),

    -- Austrian socialists and Austromarxists (44-49)
    ('Victor', 'Adler', 'victor.adler@example.com', '+43-1-1852', '1852-06-24', 1, 2, 'system', 'system'),
    ('Otto', 'Bauer', 'otto.bauer@example.com', '+43-1-1881', '1881-09-05', 1, 2, 'system', 'system'),
    ('Bruno', 'Kreisky', 'bruno.kreisky@example.com', '+43-1-1911', '1911-01-22', 2, 2, 'system', 'system'),
    ('Rudolf', 'Hilferding', 'rudolf.hilferding@example.com', '+43-1-1877', '1877-08-10', 1, 2, 'system', 'system'),
    ('Karl', 'Renner', 'karl.renner@example.com', '+43-1-1870', '1870-12-14', 1, 2, 'system', 'system'),
    ('Max', 'Adler', 'max.adler@example.com', '+43-1-1873', '1873-01-15', 1, 2, 'system', 'system'),

    -- Portuguese figures (50-51)
    ('Mario', 'Soares', 'mario.soares@example.com', '+351-21-1924', '1924-12-07', 1, 2, 'system', 'system'),
    ('Alvaro', 'Cunhal', 'alvaro.cunhal@example.com', '+351-21-1913', '1913-11-10', 2, 2, 'system', 'system'),

    -- Dutch socialists (52-54)
    ('Pieter Jelles', 'Troelstra', 'pieter.troelstra@example.com', '+31-70-1860', '1860-04-20', 2, 2, 'system', 'system'),
    ('Willem', 'Drees', 'willem.drees@example.com', '+31-70-1886', '1886-07-05', 2, 2, 'system', 'system'),
    ('Joop', 'den Uyl', 'joop.denuyl@example.com', '+31-70-1919', '1919-08-09', 2, 2, 'system', 'system'),

    -- Belgian socialists (55-56)
    ('Emile', 'Vandervelde', 'emile.vandervelde@example.com', '+32-2-1866', '1866-01-25', 2, 2, 'system', 'system'),
    ('Paul-Henri', 'Spaak', 'paulhenri.spaak@example.com', '+32-2-1899', '1899-01-25', 2, 2, 'system', 'system'),

    -- Greek socialist (57)
    ('Andreas', 'Papandreou', 'andreas.papandreou@example.com', '+30-21-1919', '1919-02-05', 1, 2, 'system', 'system'),

    -- Spanish socialists (58-60)
    ('Felipe', 'Gonzalez', 'felipe.gonzalez@example.com', '+34-91-1942', '1942-03-05', 2, 2, 'system', 'system'),
    ('Pablo Iglesias', 'Posse', 'pabloiglesias.posse@example.com', '+34-91-1850', '1850-10-17', 2, 2, 'system', 'system'),
    ('Dolores', 'Ibarruri', 'dolores.ibarruri@example.com', '+34-94-1895', '1895-12-09', 4, 1, 'system', 'system'),

    -- Russian/Soviet figures (61-62)
    ('Alexandra', 'Kollontai', 'alexandra.kollontai@example.com', '+7-495-1872', '1872-03-31', 4, 1, 'system', 'system'),
    ('Inessa', 'Armand', 'inessa.armand@example.com', '+7-495-1874', '1874-05-08', 4, 1, 'system', 'system')
;
