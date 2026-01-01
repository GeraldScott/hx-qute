-- Insert person relationships based on historical connections among European left-wing activists
-- Person IDs reference V1.4.1 (1-62)
-- Relationship IDs: 1=SPOUSE, 2=PARENT, 3=CHILD, 4=SIBLING, 5=COLLEAGUE, 6=FRIEND,
--                   7=MENTOR, 8=MENTEE, 9=ALLY, 10=COLLAB, 11=SUCCESSOR, 12=PREDEC, 13=RIVAL

-- Person ID mapping (for reference):
-- 1=Marx, 2=Engels, 3=Rosa Luxemburg, 4=Clara Zetkin, 5=Gramsci, 6=Sartre, 7=de Beauvoir,
-- 8=Tony Benn, 9=Berlinguer, 10=Olof Palme, 11=Varoufakis, 12=Pablo Iglesias (modern),
-- 13=Jaurès, 14=Blum, 15=Mitterrand, 16=Fourier, 17=Lafargue, 18=Guesde, 19=Bebel,
-- 20=W.Liebknecht, 21=K.Liebknecht, 22=Brandt, 23=Schmidt, 24=Lassalle, 25=Kautsky,
-- 26=Bernstein, 27=Marcuse, 28=Togliatti, 29=Nenni, 30=Hardie, 31=Attlee, 32=Bevan,
-- 33=M.Foot, 34=Wilson, 35=Kinnock, 36=Owen, 37=Hobsbawm, 38=Thompson, 39=S.Pankhurst,
-- 40=E.Marx, 41=Hansson, 42=Erlander, 43=Brundtland, 44=V.Adler, 45=O.Bauer, 46=Kreisky,
-- 47=Hilferding, 48=Renner, 49=M.Adler, 50=Soares, 51=Cunhal, 52=Troelstra, 53=Drees,
-- 54=den Uyl, 55=Vandervelde, 56=Spaak, 57=Papandreou, 58=F.González, 59=P.I.Posse,
-- 60=Ibárruri, 61=Kollontai, 62=Armand

INSERT INTO person_relationship (source_person_id, related_person_id, relationship_id, created_by, updated_by) VALUES
    -- === THE FOUNDERS: Marx, Engels, and their circle ===
    -- Marx & Engels: lifelong collaborators and friends (Communist Manifesto, Das Kapital)
    (1, 2, 10, 'system', 'system'),  -- Marx COLLABORATOR Engels
    (1, 2, 6, 'system', 'system'),   -- Marx FRIEND Engels
    (2, 1, 10, 'system', 'system'),  -- Engels COLLABORATOR Marx
    (2, 1, 6, 'system', 'system'),   -- Engels FRIEND Marx

    -- Marx family relationships
    (1, 40, 2, 'system', 'system'),  -- Marx PARENT Eleanor Marx
    (40, 1, 3, 'system', 'system'),  -- Eleanor Marx CHILD Marx
    (40, 2, 7, 'system', 'system'),  -- Eleanor Marx had Engels as MENTOR (he oversaw her work on Das Kapital)

    -- Paul Lafargue married Laura Marx (Karl's daughter) - colleague relationship
    (1, 17, 5, 'system', 'system'),  -- Marx COLLEAGUE Lafargue
    (17, 1, 8, 'system', 'system'),  -- Lafargue MENTEE Marx

    -- === GERMAN SPD FOUNDERS ===
    -- Bebel & Wilhelm Liebknecht: co-founded SPD
    (19, 20, 10, 'system', 'system'), -- Bebel COLLABORATOR W.Liebknecht
    (19, 20, 6, 'system', 'system'),  -- Bebel FRIEND W.Liebknecht
    (20, 19, 10, 'system', 'system'), -- W.Liebknecht COLLABORATOR Bebel
    (20, 19, 6, 'system', 'system'),  -- W.Liebknecht FRIEND Bebel

    -- Wilhelm Liebknecht was Karl Liebknecht's father
    (20, 21, 2, 'system', 'system'), -- W.Liebknecht PARENT K.Liebknecht
    (21, 20, 3, 'system', 'system'), -- K.Liebknecht CHILD W.Liebknecht

    -- Lassalle founded competing socialist movement, merged with Bebel's
    (24, 19, 13, 'system', 'system'), -- Lassalle RIVAL Bebel
    (19, 24, 13, 'system', 'system'), -- Bebel RIVAL Lassalle

    -- Bebel & Liebknecht were close to Marx/Engels
    (19, 1, 8, 'system', 'system'),  -- Bebel MENTEE Marx
    (19, 2, 8, 'system', 'system'),  -- Bebel MENTEE Engels
    (20, 1, 6, 'system', 'system'),  -- W.Liebknecht FRIEND Marx

    -- === GERMAN MARXIST THEORISTS ===
    -- Kautsky & Bernstein: colleagues who became rivals over revisionism
    (25, 26, 5, 'system', 'system'),  -- Kautsky COLLEAGUE Bernstein
    (25, 26, 13, 'system', 'system'), -- Kautsky RIVAL Bernstein (over revisionism)
    (26, 25, 5, 'system', 'system'),  -- Bernstein COLLEAGUE Kautsky
    (26, 25, 13, 'system', 'system'), -- Bernstein RIVAL Kautsky

    -- Rosa Luxemburg debated Kautsky and Bernstein
    (3, 25, 13, 'system', 'system'),  -- Rosa RIVAL Kautsky
    (3, 26, 13, 'system', 'system'),  -- Rosa RIVAL Bernstein

    -- === SPARTACIST LEAGUE: Rosa Luxemburg & Karl Liebknecht ===
    (3, 21, 9, 'system', 'system'),  -- Rosa POLITICAL_ALLY K.Liebknecht
    (3, 21, 5, 'system', 'system'),  -- Rosa COLLEAGUE K.Liebknecht
    (21, 3, 9, 'system', 'system'),  -- K.Liebknecht POLITICAL_ALLY Rosa
    (21, 3, 5, 'system', 'system'),  -- K.Liebknecht COLLEAGUE Rosa

    -- Rosa Luxemburg & Clara Zetkin: close friends and political allies
    (3, 4, 6, 'system', 'system'),   -- Rosa FRIEND Clara
    (3, 4, 9, 'system', 'system'),   -- Rosa POLITICAL_ALLY Clara
    (4, 3, 6, 'system', 'system'),   -- Clara FRIEND Rosa
    (4, 3, 9, 'system', 'system'),   -- Clara POLITICAL_ALLY Rosa

    -- === GERMAN CHANCELLORS (SPD) ===
    -- Willy Brandt succeeded by Helmut Schmidt
    (22, 23, 12, 'system', 'system'), -- Brandt PREDECESSOR Schmidt
    (23, 22, 11, 'system', 'system'), -- Schmidt SUCCESSOR Brandt
    (22, 23, 5, 'system', 'system'),  -- Brandt COLLEAGUE Schmidt

    -- === FRENCH SOCIALISTS ===
    -- Jaurès mentored Blum
    (13, 14, 7, 'system', 'system'),  -- Jaurès MENTOR Blum
    (14, 13, 8, 'system', 'system'),  -- Blum MENTEE Jaurès

    -- Jaurès & Guesde: rivals in French socialism
    (13, 18, 13, 'system', 'system'), -- Jaurès RIVAL Guesde
    (18, 13, 13, 'system', 'system'), -- Guesde RIVAL Jaurès

    -- Blum to Mitterrand: French socialist lineage
    (14, 15, 12, 'system', 'system'), -- Blum PREDECESSOR Mitterrand

    -- === FRENCH EXISTENTIALISTS ===
    -- Sartre & de Beauvoir: lifelong partners
    (6, 7, 1, 'system', 'system'),   -- Sartre SPOUSE de Beauvoir
    (7, 6, 1, 'system', 'system'),   -- de Beauvoir SPOUSE Sartre
    (6, 7, 10, 'system', 'system'),  -- Sartre COLLABORATOR de Beauvoir
    (7, 6, 10, 'system', 'system'),  -- de Beauvoir COLLABORATOR Sartre

    -- === ITALIAN COMMUNISTS ===
    -- Gramsci & Togliatti: co-founders of Italian Communist Party
    (5, 28, 5, 'system', 'system'),  -- Gramsci COLLEAGUE Togliatti
    (5, 28, 9, 'system', 'system'),  -- Gramsci POLITICAL_ALLY Togliatti
    (28, 5, 5, 'system', 'system'),  -- Togliatti COLLEAGUE Gramsci
    (28, 5, 9, 'system', 'system'),  -- Togliatti POLITICAL_ALLY Gramsci

    -- Togliatti & Nenni: allies in Popular Front
    (28, 29, 9, 'system', 'system'), -- Togliatti POLITICAL_ALLY Nenni
    (29, 28, 9, 'system', 'system'), -- Nenni POLITICAL_ALLY Togliatti

    -- Berlinguer succeeded Togliatti
    (28, 9, 12, 'system', 'system'), -- Togliatti PREDECESSOR Berlinguer
    (9, 28, 11, 'system', 'system'), -- Berlinguer SUCCESSOR Togliatti
    (9, 28, 8, 'system', 'system'),  -- Berlinguer MENTEE Togliatti

    -- === BRITISH LABOUR ===
    -- Hardie founded Labour, Attlee led it to victory
    (30, 31, 12, 'system', 'system'), -- Hardie PREDECESSOR Attlee

    -- Attlee & Bevan: colleagues in 1945 government
    (31, 32, 5, 'system', 'system'),  -- Attlee COLLEAGUE Bevan
    (32, 31, 5, 'system', 'system'),  -- Bevan COLLEAGUE Attlee

    -- Michael Foot was Bevan's disciple
    (32, 33, 7, 'system', 'system'),  -- Bevan MENTOR M.Foot
    (33, 32, 8, 'system', 'system'),  -- M.Foot MENTEE Bevan
    (33, 32, 6, 'system', 'system'),  -- M.Foot FRIEND Bevan

    -- Wilson & Foot: colleagues
    (34, 33, 5, 'system', 'system'),  -- Wilson COLLEAGUE M.Foot
    (33, 34, 5, 'system', 'system'),  -- M.Foot COLLEAGUE Wilson

    -- Kinnock succeeded Foot
    (33, 35, 12, 'system', 'system'), -- M.Foot PREDECESSOR Kinnock
    (35, 33, 11, 'system', 'system'), -- Kinnock SUCCESSOR M.Foot

    -- Tony Benn - colleague of Foot
    (8, 33, 5, 'system', 'system'),   -- Benn COLLEAGUE M.Foot
    (8, 33, 9, 'system', 'system'),   -- Benn POLITICAL_ALLY M.Foot

    -- === BRITISH MARXIST HISTORIANS ===
    -- Hobsbawm & Thompson: colleagues and friends
    (37, 38, 5, 'system', 'system'),  -- Hobsbawm COLLEAGUE Thompson
    (37, 38, 6, 'system', 'system'),  -- Hobsbawm FRIEND Thompson
    (38, 37, 5, 'system', 'system'),  -- Thompson COLLEAGUE Hobsbawm
    (38, 37, 6, 'system', 'system'),  -- Thompson FRIEND Hobsbawm

    -- === SWEDISH SOCIAL DEMOCRATS ===
    -- Erlander succeeded Hansson
    (41, 42, 12, 'system', 'system'), -- Hansson PREDECESSOR Erlander
    (42, 41, 11, 'system', 'system'), -- Erlander SUCCESSOR Hansson

    -- Palme succeeded Erlander
    (42, 10, 12, 'system', 'system'), -- Erlander PREDECESSOR Palme
    (10, 42, 11, 'system', 'system'), -- Palme SUCCESSOR Erlander
    (42, 10, 7, 'system', 'system'),  -- Erlander MENTOR Palme
    (10, 42, 8, 'system', 'system'),  -- Palme MENTEE Erlander

    -- Brandt & Palme: friends and fellow social democrats
    (22, 10, 6, 'system', 'system'),  -- Brandt FRIEND Palme
    (10, 22, 6, 'system', 'system'),  -- Palme FRIEND Brandt
    (22, 10, 9, 'system', 'system'),  -- Brandt POLITICAL_ALLY Palme
    (10, 22, 9, 'system', 'system'),  -- Palme POLITICAL_ALLY Brandt

    -- === AUSTROMARXISTS ===
    -- Victor Adler founded Austrian social democracy, Otto Bauer succeeded
    (44, 45, 7, 'system', 'system'),  -- V.Adler MENTOR O.Bauer
    (45, 44, 8, 'system', 'system'),  -- O.Bauer MENTEE V.Adler
    (44, 45, 12, 'system', 'system'), -- V.Adler PREDECESSOR O.Bauer
    (45, 44, 11, 'system', 'system'), -- O.Bauer SUCCESSOR V.Adler

    -- Bauer mentored Kreisky
    (45, 46, 7, 'system', 'system'),  -- O.Bauer MENTOR Kreisky
    (46, 45, 8, 'system', 'system'),  -- Kreisky MENTEE O.Bauer

    -- Hilferding, Renner, Max Adler: Austromarxist colleagues
    (45, 47, 5, 'system', 'system'),  -- O.Bauer COLLEAGUE Hilferding
    (47, 45, 5, 'system', 'system'),  -- Hilferding COLLEAGUE O.Bauer
    (45, 48, 5, 'system', 'system'),  -- O.Bauer COLLEAGUE Renner
    (48, 45, 5, 'system', 'system'),  -- Renner COLLEAGUE O.Bauer
    (45, 49, 5, 'system', 'system'),  -- O.Bauer COLLEAGUE M.Adler
    (49, 45, 5, 'system', 'system'),  -- M.Adler COLLEAGUE O.Bauer

    -- === PORTUGUESE RIVALS ===
    -- Soares & Cunhal: rivals after Carnation Revolution
    (50, 51, 13, 'system', 'system'), -- Soares RIVAL Cunhal
    (51, 50, 13, 'system', 'system'), -- Cunhal RIVAL Soares

    -- === DUTCH SOCIALISTS ===
    -- Drees was influenced by Troelstra
    (52, 53, 7, 'system', 'system'),  -- Troelstra MENTOR Drees
    (53, 52, 8, 'system', 'system'),  -- Drees MENTEE Troelstra

    -- Den Uyl succeeded Drees's tradition
    (53, 54, 12, 'system', 'system'), -- Drees PREDECESSOR den Uyl

    -- === BELGIAN SOCIALISTS ===
    -- Vandervelde & Spaak: colleagues, later rivals
    (55, 56, 5, 'system', 'system'),  -- Vandervelde COLLEAGUE Spaak
    (55, 56, 13, 'system', 'system'), -- Vandervelde RIVAL Spaak
    (56, 55, 5, 'system', 'system'),  -- Spaak COLLEAGUE Vandervelde

    -- === SPANISH SOCIALISTS ===
    -- Pablo Iglesias Posse founded PSOE, Felipe González modernized it
    (59, 58, 12, 'system', 'system'), -- P.I.Posse PREDECESSOR F.González

    -- Ibárruri (La Pasionaria) - Spanish Communist
    (60, 59, 9, 'system', 'system'),  -- Ibárruri POLITICAL_ALLY P.I.Posse (in Civil War era)

    -- === BOLSHEVIK WOMEN ===
    -- Kollontai & Armand: colleagues and friends
    (61, 62, 5, 'system', 'system'),  -- Kollontai COLLEAGUE Armand
    (61, 62, 6, 'system', 'system'),  -- Kollontai FRIEND Armand
    (62, 61, 5, 'system', 'system'),  -- Armand COLLEAGUE Kollontai
    (62, 61, 6, 'system', 'system'),  -- Armand FRIEND Kollontai

    -- Kollontai & Clara Zetkin: international socialist women's movement
    (61, 4, 6, 'system', 'system'),   -- Kollontai FRIEND Clara
    (61, 4, 9, 'system', 'system'),   -- Kollontai POLITICAL_ALLY Clara
    (4, 61, 6, 'system', 'system'),   -- Clara FRIEND Kollontai
    (4, 61, 9, 'system', 'system'),   -- Clara POLITICAL_ALLY Kollontai

    -- Sylvia Pankhurst & Kollontai: international connections
    (39, 61, 6, 'system', 'system'),  -- S.Pankhurst FRIEND Kollontai
    (39, 4, 9, 'system', 'system'),   -- S.Pankhurst POLITICAL_ALLY Clara

    -- Sylvia Pankhurst knew Gramsci
    (39, 5, 5, 'system', 'system'),   -- S.Pankhurst COLLEAGUE Gramsci

    -- === INTERNATIONAL CONNECTIONS ===
    -- Second International leaders knew each other
    (55, 13, 5, 'system', 'system'),  -- Vandervelde COLLEAGUE Jaurès (Second International)
    (55, 19, 5, 'system', 'system'),  -- Vandervelde COLLEAGUE Bebel
    (13, 19, 5, 'system', 'system'),  -- Jaurès COLLEAGUE Bebel

    -- Modern European left connections
    (11, 12, 9, 'system', 'system'),  -- Varoufakis POLITICAL_ALLY P.Iglesias (DiEM25)
    (12, 11, 9, 'system', 'system')   -- P.Iglesias POLITICAL_ALLY Varoufakis
;
