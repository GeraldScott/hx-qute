-- Insert common relationship type codes
-- Note: code column is VARCHAR(10)
INSERT INTO relationship (code, description) VALUES
    ('SPOUSE', 'Spouse'),
    ('PARENT', 'Parent'),
    ('CHILD', 'Child'),
    ('SIBLING', 'Sibling'),
    ('COLLEAGUE', 'Colleague'),
    ('FRIEND', 'Friend'),
    ('MENTOR', 'Mentor'),
    ('MENTEE', 'Mentee'),
    ('ALLY', 'Political Ally'),
    ('COLLAB', 'Collaborator'),
    ('SUCCESSOR', 'Successor'),
    ('PREDEC', 'Predecessor'),
    ('RIVAL', 'Rival'),
    ('STUDENT', 'Student'),
    ('TEACHER', 'Teacher')
;
