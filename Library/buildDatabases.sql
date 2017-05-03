CREATE DATABASE OOLibrary;
USE OOLibrary 
CREATE TABLE user(uniformID VARCHAR(20), hasRight Boolean, isKeeper Boolean);
CREATE TABLE book(BookID VARCHAR(128), name VARCHAR(128), information VARCHAR(1024), state int, returnTime date, borrowedUserID VARCHAR(20));
CREATE TABLE seat(SeatID VARCHAR(128), floor int, state int, bookInfor VARCHAR(4096));
CREATE TABLE board(startTime date, endTime date, editorID VARCHAR(20), information VARCHAR(4096));

insert into user (uniformID,hasRight,isKeeper) values('userID',true,false);

insert into book (BookID,name,information,state,returnTime,borrowedUserID) values('bID123','name','infor','1','1992-11-2','buid');

