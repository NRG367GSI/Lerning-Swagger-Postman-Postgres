select * from student;

-- Получить всех студентов, возраст которых находится между 10 и 20
select * from student where age > 10 and age < 20; -- способ 1
select * from student where age between 10 and 20 order by age; -- более правельный способ и по возростанию
select * from student where age between 10 and 20 order by age desc; -- с сортировкой по убыванию

--Получить всех студентов, но отобразить только список их имен
select name  from student order by name; -- с сортировеой по имени

-- Получить всех студентов, у которых в имени присутствует буква О
select * from student where lower(name) like lower('%O%') order by name; -- применил lowercase и сортировку

-- Получить всех студентов, у которых возраст меньше идентификатора (id)
select * from student where age < id order by age;

--Получить всех студентов упорядоченных по возрасту
select * from student order by age;
select * from student order by age desc;