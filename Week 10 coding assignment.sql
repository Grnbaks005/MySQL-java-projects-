use projects;

INSERT INTO category (category_name) VALUES ('Doors and Windows');
SELECT * FROM project;
INSERT INTO material (project_id, material_name, num_required)
VALUES
(1, '2-inch screws', 20);
INSERT INTO material (project_id, material_name, num_required)
VALUES
(1, '4 inch door hinge', 5);
INSERT INTO material (project_id, material_name, num_required)
VALUES
(1, 'door frames', 2);

INSERT INTO step (project_id, step_text, step_order)
VALUES
(1, 'Put up door frames ', 1);

INSERT INTO step (project_id, step_text, step_order)
VALUES
(1, 'Screw door hangers on the top and bottom of each side of the door frame', 2);
INSERT INTO step (project_id, step_text, step_order)
VALUES
(1, 'Align and hang the door', 3);

INSERT INTO project_category (project_id, category_id)
VALUES
(1,2);
