
/*
 * Claim table 
 */
alter table {schema}.claim add column key_ins varchar (7);
update {schema}.claim set key_ins = substring (pol_idx from 1 for 7);
create index claim_i_pol on {schema}.claim (key_ins);
/*
 * Delete any blank claim numbers and create an index on claimrec.
 */
delete from {schema}.claim where claimrec = '      ';
create unique index claim_i_rec on {schema}.claim (claimrec);

/**
 * FLDS Table.
 */
create index flds_i on {schema}.flds (fld, keyvalue);

create table {schema}.cov_types as select trim (trailing ' ' from keyvalue) as keyvalue, trim (trailing ' ' from descrip) as descrip from {schema}.flds where fld = 'B';
create index cov_typesi1 on {schema}.cov_types (keyvalue);

create table {schema}.csr as select trim (trailing ' ' from keyvalue) as keyvalue, trim (trailing ' ' from descrip) as descrip from {schema}.flds where fld = 1;
create index csri1 on {schema}.cov_types (csr);
/**
 * INS Table.  This generates the producer, broker, vendor, customer, department, and company tables based on the key value.
 */
update {schema}.ins set rec =  trim (trailing ' ' from rec);
create index ins_i_rec on {schema}.ins (key);

create table {schema}.producer as select * from {schema}.ins where key = 'P';
create index producer_i_name on {schema}.producer (name);
create index ins_i_rec on {schema}.producer (rec);

create table {schema}.broker as select * from {schema}.ins where key = 'B';
create index broker_i_name on {schema}.broker (name);
create index broker_i_rec on {schema}.broker (rec);

create table {schema}.vendor as select * from {schema}.ins where key = 'V';
create index vendor_i_name on {schema}.vendor (name);
create index vendor_i_rec on {schema}.vendor (rec);

create table {schema}.customer as select 
	_rownum, 
	key, 
	rec, 
	agcy, 
	brch, 
	name, 
	attn, 
	street, 
	street2, 
	city, 
	st, 
	zip, 
	phr, 
	phb, 
	info_n, 
	follow_n, 
	note_n, 
	bal, 
	bal1, 
	broker, 
	trim (substring (c,1,2))	::varchar(2) as csr, 
	trim (substring (c,3,2))	::varchar (2) as code, 
	trim (substring (c,5,15))	::varchar (15) as ocp, 
	trim (substring (c,20,20))	::varchar (20) as hdg, 
	trim (substring (c, 40,17))	::varchar (17) as note, 
	trim (substring (c, 63, 12))::varchar (12) as fax,
	trim (substring (c, 209, 3))::varchar (3) as prod 
	from {schema}.ins where key = 'C';

/*
 * Customer
 */
create index customer_i_name on {schema}.customer (name);
create index customer_i_rec on {schema}.customer (rec);

/*
 * Department
 */
create table {schema}.department as select * from {schema}.ins where key = 'D';
create index department_i_name on {schema}.department (name);
create index department_i_rec on {schema}.department (rec);

/*
 * Company
 */
create table {schema}.company as select * from {schema}.ins where key = 'Y';
create index company_i_name on {schema}.company (name);
create index company_i_rec on {schema}.company (rec);

/*
 * Agency table.
 */
create table {schema}.agency as select * from {schema}.ins where key = 'A';
create index agency_i_name on {schema}.agency (name);
create index agency_i_rec on {schema}.agency (rec);

/*
 * Branch table.
 */
create table {schema}.branch as select * from {schema}.ins where key = 'H';
create index branch_i_rec on {schema}.branch (rec);

/*
 * Policy table.
 */
delete from {schema}.policy where pol_idx like '.PICTUR%' or pol_idx like '% %';
update {schema}.policy set pol = trim(pol);

alter table {schema}.policy add column key_ins varchar (7);
update {schema}.policy set key_ins = substring (pol_idx from 1 for 7);

alter table {schema}.policy add column key_pol varchar (7);
update {schema}.policy set key_pol = substring (pol_idx from 8);

create index policy_i_pol on {schema}.policy (key_ins, key_pol);

alter table {schema}.policy add column key_poli int;
update {schema}.policy set key_poli = trim (leading '-' from key_pol)::int;
create index policy_i_poli on {schema}.policy (key_ins, key_poli);

create unique index policy_pol_idx_i on {schema}.policy (pol_idx);


/*
 * Prospect.
 */
create unique index prospect_i on {schema}.prospect (rec);



/*
 * Transactions.
 */
alter table {schema}.transact add column key_ins varchar (7);
update {schema}.transact set key_ins = substring (pol_idx from 1 for 7);

alter table {schema}.transact add column key_pol varchar (7);
update {schema}.transact set key_pol = substring (pol_idx from 8);

create index transact_i_pol on {schema}.transact (key_ins, key_pol);

alter table {schema}.transact add column key_poli int;
update {schema}.transact set key_poli = case when trim(trim (leading '-' from key_pol)) = '' then null else
	trim (leading '-' from key_pol)::int end; 
create index transact_poli on {schema}.transact (key_ins, key_poli);
delete from {schema}.transact where key_poli is null;
