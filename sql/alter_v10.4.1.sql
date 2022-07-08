alter table ai_users add column filter varchar (256);

create table ai_office_queue (
    id 			int,
    userid 		int,
    status 		char(1),
    starttime 	datetime
);

create unique index ai_office_queuei1 on ai_office_queue (id);

create table ai_office_runlog (
    id 			int,
    userid 		int,
    status 		char(1),
    starttime 	datetime,
    endtime 	datetime
);


/* Add a sequencing key for running the extracts so the copies run after
 * the extracted offices.  Copies of copies will be a problem.
 */
alter table ai_office_type add column seq int;
update ai_office_type set seq = 1;
update ai_office_type set seq = 10 where id = 'C';


insert into ai_authrole (name, description, grouping) values ('run', 'Run Offices', 'Data');
alter table ai_office add column noextract char(1);


create table ai_office_security
(
	office_id			int,
	user_id				int,
	status				char(1), /* A=Access, D=Denied */
	filter				varchar (512)
)
;

create unique index ai_office_securityi1 on ai_office_security (office_id, user_id);

create table ai_kpi_export_prefix_map 
(
	id					char (1),
	name				varchar (60),
	shortname			varchar (10)
)
;

create unique index ai_kpi_export_prefix_map_i1 on ai_kpi_export_prefix_map (id);
insert into ai_kpi_export_prefix_map values ('N', 'New Business', 'New');
insert into ai_kpi_export_prefix_map values ('L', 'Lost Business', 'Lost');
insert into ai_kpi_export_prefix_map values ('R', 'Renewal Business', 'Renew');
