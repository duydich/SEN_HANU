USE demo_2301140019;
SELECT id,name,age FROM student ORDER BY id;
SELECT id,name,address FROM company ORDER BY id;
SELECT id,title,description FROM job ORDER BY id;
SELECT e.id,e.name,e.age,e.email,c.name AS company
FROM employee e LEFT JOIN company c ON e.company_id=c.id ORDER BY e.id;
SELECT e.name AS employee,j.title AS job
FROM employee_job ej JOIN employee e ON ej.employee_id=e.id JOIN job j ON ej.job_id=j.id;
-- Never expose the password hashes as API data.
SELECT id,username,role FROM app_user;
