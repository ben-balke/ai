/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/sicmajor.sql,v 1.2 2010/10/18 19:04:37 secwind Exp $
 * Description:
 *      SIC Codes
 */

drop table cd_sicmajor
;
create table cd_sicmajor 
(
	id		varchar (2),
	name	varchar (128)
)
;
insert into cd_sicmajor values (NULL,'NOT SET');
insert into cd_sicmajor values ('','UNDEFINED');
insert into cd_sicmajor values ('01','Agricultural Production Crops ');
insert into cd_sicmajor values ('02','Agriculture production livestock and animal specialties ');
insert into cd_sicmajor values ('07','Agricultural Services ');
insert into cd_sicmajor values ('08','Forestry ');
insert into cd_sicmajor values ('09','Fishing, hunting, and trapping ');
insert into cd_sicmajor values ('10','Metal Mining  ');
insert into cd_sicmajor values ('12','Coal Mining ');
insert into cd_sicmajor values ('13','Oil And Gas Extraction ');
insert into cd_sicmajor values ('14','Mining And Quarrying Of Nonmetallic Minerals, Except Fuels ');
insert into cd_sicmajor values ('15','Building Construction General Contractors And Operative Builders ');
insert into cd_sicmajor values ('16','Heavy Construction Other Than Building Construction Contractors ');
insert into cd_sicmajor values ('17','Construction Special Trade Contractors ');
insert into cd_sicmajor values ('20','Food And Kindred Products ');
insert into cd_sicmajor values ('21','Tobacco Products ');
insert into cd_sicmajor values ('22','Textile Mill Products ');
insert into cd_sicmajor values ('23','Apparel And Other Finished Products Made From Fabrics And Similar Materials ');
insert into cd_sicmajor values ('24','Lumber And Wood Products, Except Furniture ');
insert into cd_sicmajor values ('25','Furniture And Fixtures ');
insert into cd_sicmajor values ('26','Paper And Allied Products ');
insert into cd_sicmajor values ('27','Printing, Publishing, And Allied Industries ');
insert into cd_sicmajor values ('28','Chemicals And Allied Products ');
insert into cd_sicmajor values ('29','Petroleum Refining And Related Industries ');
insert into cd_sicmajor values ('30','Rubber And Miscellaneous Plastics Products ');
insert into cd_sicmajor values ('31','Leather And Leather Products ');
insert into cd_sicmajor values ('32','Stone, Clay, Glass, And Concrete Products ');
insert into cd_sicmajor values ('33','Primary Metal Industries ');
insert into cd_sicmajor values ('34','Fabricated Metal Products, Except Machinery And Transportation Equipment ');
insert into cd_sicmajor values ('35','Industrial And Commercial Machinery And Computer Equipment ');
insert into cd_sicmajor values ('36','Electronic And Other Electrical Equipment And Components, Except Computer Equipment ');
insert into cd_sicmajor values ('37','Transportation Equipment ');
insert into cd_sicmajor values ('38','Measuring, Analyzing, And Controlling Instruments; Photographic, Medical And Optical Goods; Watches And Clocks ');
insert into cd_sicmajor values ('39','Miscellaneous Manufacturing Industries ');
insert into cd_sicmajor values ('40','Railroad Transportation ');
insert into cd_sicmajor values ('41','Local And Suburban Transit And Interurban Highway Passenger Transportation ');
insert into cd_sicmajor values ('42','Motor Freight Transportation And Warehousing ');
insert into cd_sicmajor values ('43','United States Postal Service ');
insert into cd_sicmajor values ('44','Water Transportation ');
insert into cd_sicmajor values ('45','Transportation By Air ');
insert into cd_sicmajor values ('46','Pipelines, Except Natural Gas ');
insert into cd_sicmajor values ('47','Transportation Services ');
insert into cd_sicmajor values ('48','Communications ');
insert into cd_sicmajor values ('49','Electric, Gas, And Sanitary Services ');
insert into cd_sicmajor values ('50','Wholesale Trade-durable Goods ');
insert into cd_sicmajor values ('51','Wholesale Trade-non-durable Goods ');
insert into cd_sicmajor values ('52','Building Materials, Hardware, Garden Supply, And Mobile Home Dealers ');
insert into cd_sicmajor values ('53','General Merchandise Stores ');
insert into cd_sicmajor values ('54','Food Stores ');
insert into cd_sicmajor values ('55','Automotive Dealers And Gasoline Service Stations ');
insert into cd_sicmajor values ('56','Apparel And Accessory Stores ');
insert into cd_sicmajor values ('57','Home Furniture, Furnishings, And Equipment Stores ');
insert into cd_sicmajor values ('58','Eating And Drinking Places ');
insert into cd_sicmajor values ('59','Miscellaneous Retail ');
insert into cd_sicmajor values ('60','Depository Institutions ');
insert into cd_sicmajor values ('61','Non-depository Credit Institutions ');
insert into cd_sicmajor values ('62','Security And Commodity Brokers, Dealers, Exchanges, And Services ');
insert into cd_sicmajor values ('63','Insurance Carriers ');
insert into cd_sicmajor values ('64','Insurance Agents, Brokers, And Service ');
insert into cd_sicmajor values ('65','Real Estate ');
insert into cd_sicmajor values ('67','Holding And Other Investment Offices ');
insert into cd_sicmajor values ('70','Hotels, Rooming Houses, Camps, And Other Lodging Places ');
insert into cd_sicmajor values ('72','Personal Services ');
insert into cd_sicmajor values ('73','Business Services ');
insert into cd_sicmajor values ('75','Automotive Repair, Services, And Parking ');
insert into cd_sicmajor values ('76','Miscellaneous Repair Services ');
insert into cd_sicmajor values ('78','Motion Pictures ');
insert into cd_sicmajor values ('79','Amusement And Recreation Services ');
insert into cd_sicmajor values ('80','Health Services ');
insert into cd_sicmajor values ('81','Legal Services ');
insert into cd_sicmajor values ('82','Educational Services ');
insert into cd_sicmajor values ('83','Social Services ');
insert into cd_sicmajor values ('84','Museums, Art Galleries, And Botanical And Zoological Gardens ');
insert into cd_sicmajor values ('86','Membership Organizations ');
insert into cd_sicmajor values ('87','Engineering, Accounting, Research, Management, And Related Services ');
insert into cd_sicmajor values ('88','Private Households ');
insert into cd_sicmajor values ('89','Miscellaneous Services ');
insert into cd_sicmajor values ('91','Executive, Legislative, And General Government, Except Finance ');
insert into cd_sicmajor values ('92','Justice, Public Order, And Safety ');
insert into cd_sicmajor values ('93','Public Finance, Taxation, And Monetary Policy ');
insert into cd_sicmajor values ('94','Administration Of Human Resource Programs ');
insert into cd_sicmajor values ('95','Administration Of Environmental Quality And Housing Programs ');
insert into cd_sicmajor values ('96','Administration Of Economic Programs ');
insert into cd_sicmajor values ('97','National Security And International Affairs ');
insert into cd_sicmajor values ('99','Nonclassifiable Establishments ');
