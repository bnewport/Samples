-- phpMyAdmin SQL Dump
-- version 3.2.0.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 03, 2009 at 10:58 PM
-- Server version: 5.1.37
-- PHP Version: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `chirp`
--

-- --------------------------------------------------------

--
-- Table structure for table `LISTHEADSTRINGLONG`
--

CREATE TABLE IF NOT EXISTS `LISTHEADSTRINGLONG` (
  `KEYZ` varchar(64) NOT NULL,
  `LISTID` bigint(20) NOT NULL,
  `MIN` bigint(20) NOT NULL,
  `MAX` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `LISTITEMSTRINGLONG`
--

CREATE TABLE IF NOT EXISTS `LISTITEMSTRINGLONG` (
  `KEYZ` varchar(64) NOT NULL,
  `POS` bigint(20) NOT NULL,
  `VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`,`POS`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `LONGLONGPROPERTY`
--

CREATE TABLE IF NOT EXISTS `LONGLONGPROPERTY` (
  `KEYZ` bigint(20) NOT NULL,
  `VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `LONGSTRINGPROPERTY`
--

CREATE TABLE IF NOT EXISTS `LONGSTRINGPROPERTY` (
  `KEYZ` bigint(20) NOT NULL,
  `VALUE` varchar(192) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SETHEADSTRINGLONG`
--

CREATE TABLE IF NOT EXISTS `SETHEADSTRINGLONG` (
  `KEYZ` varchar(64) NOT NULL,
  `POS` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SETITEMSTRINGLONG`
--

CREATE TABLE IF NOT EXISTS `SETITEMSTRINGLONG` (
  `KEYZ` varchar(64) NOT NULL,
  `POS` bigint(20) NOT NULL,
  `VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`,`POS`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `STRINGLONGPROPERTY`
--

CREATE TABLE IF NOT EXISTS `STRINGLONGPROPERTY` (
  `KEYZ` varchar(64) NOT NULL,
  `VALUE` bigint(20) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `STRINGSTRINGPROPERTY`
--

CREATE TABLE IF NOT EXISTS `STRINGSTRINGPROPERTY` (
  `KEYZ` varchar(64) NOT NULL,
  `VALUE` varchar(192) NOT NULL,
  PRIMARY KEY (`KEYZ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
