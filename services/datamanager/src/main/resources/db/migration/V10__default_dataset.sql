use arbiter;
insert into algorithm (id, value) value (1, 'WinRate');

insert into role (id, value, permission)
values (1, 'Owner', 100),
       (2, 'Root', 90),
       (3, 'Admin', 70),
       (4, 'RealmAdmin', 50),
       (5, 'RegisteredUser', 10);

insert into realm(id, name, team_size, selected_algorithm)
VALUES (1, 'system', 2, 1);

insert into user(id, surname, nickname, tid, active, created_at)
VALUES (1, 'маюн', null, '530809403', 1, CURRENT_TIMESTAMP());

insert into user_realm_role(role, user, realm)
VALUES (1,1,1);