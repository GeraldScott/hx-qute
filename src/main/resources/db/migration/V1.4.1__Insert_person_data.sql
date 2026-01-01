-- Insert sample person records: famous European left-wing activists
-- Gender: 1=Female, 2=Male, 3=Unspecified
-- Title: 1=DR, 2=MR, 3=MRS, 4=MS, 5=PROF, 6=REV

INSERT INTO person (first_name, last_name, email, phone, date_of_birth, title_id, gender_id, notes, created_by, updated_by) VALUES
    -- Original records (1-12)
    ('Karl', 'Marx', 'karl.marx@example.com', '+49-30-1818', '1818-05-05', 2, 2,
     'German philosopher and economist; author of Das Kapital and co-author of The Communist Manifesto.', 'system', 'system'),
    ('Friedrich', 'Engels', 'friedrich.engels@example.com', '+49-202-1820', '1820-11-28', 2, 2,
     'German philosopher and Marx''s collaborator; co-authored The Communist Manifesto and funded Marx''s work.', 'system', 'system'),
    ('Rosa', 'Luxemburg', 'rosa.luxemburg@example.com', '+49-30-1871', '1871-03-05', 4, 1,
     'Polish-German Marxist theorist; co-founded the Spartacist League and German Communist Party.', 'system', 'system'),
    ('Clara', 'Zetkin', 'clara.zetkin@example.com', '+49-711-1857', '1857-07-05', 4, 1,
     'German Marxist and women''s rights activist; organized the first International Women''s Day.', 'system', 'system'),
    ('Antonio', 'Gramsci', 'antonio.gramsci@example.com', '+39-06-1891', '1891-01-22', 2, 2,
     'Italian Marxist philosopher; developed theory of cultural hegemony while imprisoned by Mussolini.', 'system', 'system'),
    ('Jean-Paul', 'Sartre', 'jean-paul.sartre@example.com', '+33-1-1905', '1905-06-21', 2, 2,
     'French existentialist philosopher and writer; declined the 1964 Nobel Prize in Literature.', 'system', 'system'),
    ('Simone', 'de Beauvoir', 'simone.debeauvoir@example.com', '+33-1-1908', '1908-01-09', 4, 1,
     'French existentialist philosopher and feminist; author of The Second Sex.', 'system', 'system'),
    ('Tony', 'Benn', 'tony.benn@example.com', '+44-20-1925', '1925-04-03', 2, 2,
     'British Labour politician; served as Cabinet minister and championed democratic socialism.', 'system', 'system'),
    ('Enrico', 'Berlinguer', 'enrico.berlinguer@example.com', '+39-06-1922', '1922-05-25', 2, 2,
     'Italian Communist leader; architect of Eurocommunism and the Historic Compromise strategy.', 'system', 'system'),
    ('Olof', 'Palme', 'olof.palme@example.com', '+46-8-1927', '1927-01-30', 2, 2,
     'Swedish Prime Minister; architect of the Swedish welfare state, assassinated in 1986.', 'system', 'system'),
    ('Yanis', 'Varoufakis', 'yanis.varoufakis@example.com', '+30-21-1961', '1961-03-24', 1, 2,
     'Greek economist and politician; former Finance Minister and founder of DiEM25 movement.', 'system', 'system'),
    ('Pablo', 'Iglesias', 'pablo.iglesias@example.com', '+34-91-1978', '1978-10-17', 1, 2,
     'Spanish politician; co-founded Podemos party and served as Deputy Prime Minister.', 'system', 'system'),

    -- French socialists (13-18)
    ('Jean', 'Jaures', 'jean.jaures@example.com', '+33-1-1859', '1859-09-03', 2, 2,
     'French socialist leader; co-founded L''Humanite newspaper, assassinated in 1914 for opposing WWI.', 'system', 'system'),
    ('Leon', 'Blum', 'leon.blum@example.com', '+33-1-1872', '1872-04-09', 2, 2,
     'French socialist statesman; first Jewish and first socialist Prime Minister of France.', 'system', 'system'),
    ('Francois', 'Mitterrand', 'francois.mitterrand@example.com', '+33-1-1916', '1916-10-26', 2, 2,
     'French President 1981-1995; longest-serving French President, abolished death penalty.', 'system', 'system'),
    ('Charles', 'Fourier', 'charles.fourier@example.com', '+33-25-1772', '1772-04-07', 2, 2,
     'French utopian socialist; coined the word feminism and envisioned cooperative communities.', 'system', 'system'),
    ('Paul', 'Lafargue', 'paul.lafargue@example.com', '+33-1-1842', '1842-01-15', 1, 2,
     'French Marxist; Marx''s son-in-law and author of The Right to Be Lazy.', 'system', 'system'),
    ('Jules', 'Guesde', 'jules.guesde@example.com', '+33-1-1845', '1845-11-11', 2, 2,
     'French socialist leader; founded the French Workers'' Party, France''s first Marxist party.', 'system', 'system'),

    -- German socialists and theorists (19-27)
    ('August', 'Bebel', 'august.bebel@example.com', '+49-30-1840', '1840-02-22', 2, 2,
     'German socialist leader; co-founded the SPD and led it for over 40 years.', 'system', 'system'),
    ('Wilhelm', 'Liebknecht', 'wilhelm.liebknecht@example.com', '+49-30-1826', '1826-03-29', 2, 2,
     'German socialist; co-founded the SPD with Bebel, close ally of Marx in exile.', 'system', 'system'),
    ('Karl', 'Liebknecht', 'karl.liebknecht@example.com', '+49-30-1871', '1871-08-13', 1, 2,
     'German socialist; co-founded Spartacist League with Rosa Luxemburg, murdered in 1919.', 'system', 'system'),
    ('Willy', 'Brandt', 'willy.brandt@example.com', '+49-30-1913', '1913-12-18', 2, 2,
     'German Chancellor 1969-1974; won Nobel Peace Prize for Ostpolitik reconciliation with East.', 'system', 'system'),
    ('Helmut', 'Schmidt', 'helmut.schmidt@example.com', '+49-40-1918', '1918-12-23', 2, 2,
     'German Chancellor 1974-1982; pragmatic leader during oil crisis and terrorism threats.', 'system', 'system'),
    ('Ferdinand', 'Lassalle', 'ferdinand.lassalle@example.com', '+49-30-1825', '1825-04-11', 2, 2,
     'German socialist; founded first German workers'' party, died in a duel at age 39.', 'system', 'system'),
    ('Karl', 'Kautsky', 'karl.kautsky@example.com', '+49-30-1854', '1854-10-16', 1, 2,
     'Czech-Austrian Marxist theoretician; edited Marx''s Theories of Surplus Value.', 'system', 'system'),
    ('Eduard', 'Bernstein', 'eduard.bernstein@example.com', '+49-30-1850', '1850-01-06', 2, 2,
     'German social democrat; founder of evolutionary socialism and revisionist Marxism.', 'system', 'system'),
    ('Herbert', 'Marcuse', 'herbert.marcuse@example.com', '+49-30-1898', '1898-07-19', 5, 2,
     'German-American philosopher; Frankfurt School theorist, called "Father of the New Left".', 'system', 'system'),

    -- Italian communists and socialists (28-29)
    ('Palmiro', 'Togliatti', 'palmiro.togliatti@example.com', '+39-06-1893', '1893-03-26', 2, 2,
     'Italian Communist leader for 40 years; pioneered the "Italian road to socialism".', 'system', 'system'),
    ('Pietro', 'Nenni', 'pietro.nenni@example.com', '+39-06-1891', '1891-02-09', 2, 2,
     'Italian Socialist leader; Deputy Prime Minister and winner of the Stalin Peace Prize.', 'system', 'system'),

    -- British Labour and socialist figures (30-40)
    ('Keir', 'Hardie', 'keir.hardie@example.com', '+44-20-1856', '1856-08-15', 2, 2,
     'Scottish trade unionist; founder and first leader of the British Labour Party.', 'system', 'system'),
    ('Clement', 'Attlee', 'clement.attlee@example.com', '+44-20-1883', '1883-01-03', 2, 2,
     'British Prime Minister 1945-1951; created the NHS and modern welfare state.', 'system', 'system'),
    ('Aneurin', 'Bevan', 'aneurin.bevan@example.com', '+44-29-1897', '1897-11-15', 2, 2,
     'Welsh Labour politician; founder of the National Health Service in 1948.', 'system', 'system'),
    ('Michael', 'Foot', 'michael.foot@example.com', '+44-20-1913', '1913-07-23', 2, 2,
     'British Labour leader 1980-1983; journalist, author, and champion of nuclear disarmament.', 'system', 'system'),
    ('Harold', 'Wilson', 'harold.wilson@example.com', '+44-20-1916', '1916-03-11', 2, 2,
     'British Prime Minister 1964-1970 and 1974-1976; modernized Britain''s economy and society.', 'system', 'system'),
    ('Neil', 'Kinnock', 'neil.kinnock@example.com', '+44-29-1942', '1942-03-28', 2, 2,
     'British Labour leader 1983-1992; reformed the party and later served as EU Commissioner.', 'system', 'system'),
    ('Robert', 'Owen', 'robert.owen@example.com', '+44-1686-1771', '1771-05-14', 2, 2,
     'Welsh social reformer; pioneer of the cooperative movement and utopian socialism.', 'system', 'system'),
    ('Eric', 'Hobsbawm', 'eric.hobsbawm@example.com', '+44-20-1917', '1917-06-09', 5, 2,
     'British Marxist historian; author of The Age of Revolution trilogy and The Age of Extremes.', 'system', 'system'),
    ('Edward', 'Thompson', 'edward.thompson@example.com', '+44-20-1924', '1924-02-03', 5, 2,
     'British historian and activist; author of The Making of the English Working Class.', 'system', 'system'),
    ('Sylvia', 'Pankhurst', 'sylvia.pankhurst@example.com', '+44-20-1882', '1882-05-05', 4, 1,
     'British suffragette and socialist; founded the Workers'' Socialist Federation.', 'system', 'system'),
    ('Eleanor', 'Marx', 'eleanor.marx@example.com', '+44-20-1855', '1855-01-16', 4, 1,
     'Karl Marx''s daughter; socialist activist, translator, and pioneer of socialist feminism.', 'system', 'system'),

    -- Scandinavian social democrats (41-43)
    ('Per Albin', 'Hansson', 'peralbin.hansson@example.com', '+46-8-1885', '1885-10-28', 2, 2,
     'Swedish Prime Minister 1932-1946; architect of the folkhemmet (people''s home) welfare state.', 'system', 'system'),
    ('Tage', 'Erlander', 'tage.erlander@example.com', '+46-8-1901', '1901-06-13', 2, 2,
     'Swedish Prime Minister 1946-1969; longest-serving Swedish PM, expanded the welfare state.', 'system', 'system'),
    ('Gro Harlem', 'Brundtland', 'gro.brundtland@example.com', '+47-22-1939', '1939-04-20', 1, 1,
     'Norwegian Prime Minister; chaired the UN commission that defined sustainable development.', 'system', 'system'),

    -- Austrian socialists and Austromarxists (44-49)
    ('Victor', 'Adler', 'victor.adler@example.com', '+43-1-1852', '1852-06-24', 1, 2,
     'Austrian politician; founder and first leader of the Austrian Social Democratic Party.', 'system', 'system'),
    ('Otto', 'Bauer', 'otto.bauer@example.com', '+43-1-1881', '1881-09-05', 1, 2,
     'Austrian Marxist theorist; leading figure of Austromarxism and expert on nationalism.', 'system', 'system'),
    ('Bruno', 'Kreisky', 'bruno.kreisky@example.com', '+43-1-1911', '1911-01-22', 2, 2,
     'Austrian Chancellor 1970-1983; longest-serving Austrian Chancellor, modernized the country.', 'system', 'system'),
    ('Rudolf', 'Hilferding', 'rudolf.hilferding@example.com', '+43-1-1877', '1877-08-10', 1, 2,
     'Austrian-German Marxist economist; author of Finance Capital, twice German Finance Minister.', 'system', 'system'),
    ('Karl', 'Renner', 'karl.renner@example.com', '+43-1-1870', '1870-12-14', 1, 2,
     'Austrian statesman; first Chancellor after WWI and President after WWII.', 'system', 'system'),
    ('Max', 'Adler', 'max.adler@example.com', '+43-1-1873', '1873-01-15', 1, 2,
     'Austrian Marxist philosopher; leading Austromarxist theorist alongside Otto Bauer.', 'system', 'system'),

    -- Portuguese figures (50-51)
    ('Mario', 'Soares', 'mario.soares@example.com', '+351-21-1924', '1924-12-07', 1, 2,
     'Portuguese Prime Minister and President; father of Portuguese democracy after 1974 revolution.', 'system', 'system'),
    ('Alvaro', 'Cunhal', 'alvaro.cunhal@example.com', '+351-21-1913', '1913-11-10', 2, 2,
     'Portuguese Communist leader; resisted dictatorship for decades, imprisoned multiple times.', 'system', 'system'),

    -- Dutch socialists (52-54)
    ('Pieter Jelles', 'Troelstra', 'pieter.troelstra@example.com', '+31-70-1860', '1860-04-20', 2, 2,
     'Dutch socialist leader; founded the SDAP, unsuccessfully called for revolution in 1918.', 'system', 'system'),
    ('Willem', 'Drees', 'willem.drees@example.com', '+31-70-1886', '1886-07-05', 2, 2,
     'Dutch Prime Minister 1948-1958; architect of the Dutch welfare state, lived to age 101.', 'system', 'system'),
    ('Joop', 'den Uyl', 'joop.denuyl@example.com', '+31-70-1919', '1919-08-09', 2, 2,
     'Dutch Prime Minister 1973-1977; led the most progressive Dutch government of the 20th century.', 'system', 'system'),

    -- Belgian socialists (55-56)
    ('Emile', 'Vandervelde', 'emile.vandervelde@example.com', '+32-2-1866', '1866-01-25', 2, 2,
     'Belgian socialist; President of the Second International 1900-1918 and Belgian minister.', 'system', 'system'),
    ('Paul-Henri', 'Spaak', 'paulhenri.spaak@example.com', '+32-2-1899', '1899-01-25', 2, 2,
     'Belgian Prime Minister; "Father of Europe", first President of the UN General Assembly.', 'system', 'system'),

    -- Greek socialist (57)
    ('Andreas', 'Papandreou', 'andreas.papandreou@example.com', '+30-21-1919', '1919-02-05', 1, 2,
     'Greek Prime Minister; founder of PASOK, led Greece''s first socialist government.', 'system', 'system'),

    -- Spanish socialists (58-60)
    ('Felipe', 'Gonzalez', 'felipe.gonzalez@example.com', '+34-91-1942', '1942-03-05', 2, 2,
     'Spanish Prime Minister 1982-1996; modernized Spain and led it into the European Community.', 'system', 'system'),
    ('Pablo Iglesias', 'Posse', 'pabloiglesias.posse@example.com', '+34-91-1850', '1850-10-17', 2, 2,
     'Spanish socialist; founder of PSOE and UGT, first socialist elected to Spanish parliament.', 'system', 'system'),
    ('Dolores', 'Ibarruri', 'dolores.ibarruri@example.com', '+34-94-1895', '1895-12-09', 4, 1,
     'Spanish Communist leader; known as La Pasionaria, famous for "No pasaran!" slogan.', 'system', 'system'),

    -- Russian/Soviet figures (61-62)
    ('Alexandra', 'Kollontai', 'alexandra.kollontai@example.com', '+7-495-1872', '1872-03-31', 4, 1,
     'Russian revolutionary and diplomat; first female cabinet minister and ambassador in history.', 'system', 'system'),
    ('Inessa', 'Armand', 'inessa.armand@example.com', '+7-495-1874', '1874-05-08', 4, 1,
     'Russian Bolshevik revolutionary; led the Zhenotdel women''s department, close ally of Lenin.', 'system', 'system')
;
