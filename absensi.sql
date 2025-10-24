-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Apr 23, 2025 at 10:45 AM
-- Server version: 8.0.30
-- PHP Version: 8.3.11

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `absensi`
--

-- --------------------------------------------------------

--
-- Table structure for table `absensi`
--

CREATE TABLE `absensi` (
  `ID_ABSENSI` int NOT NULL,
  `TANGGAL_ABSEN` date DEFAULT NULL,
  `JAM_MASUK` time DEFAULT NULL,
  `JAM_KELUAR` time DEFAULT NULL,
  `STATUS_MASUK` char(1) DEFAULT NULL,
  `STATUS_KELUAR` char(1) DEFAULT NULL,
  `KETERANGAN` char(5) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `TERLAMBAT` char(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `absensi`
--

INSERT INTO `absensi` (`ID_ABSENSI`, `TANGGAL_ABSEN`, `JAM_MASUK`, `JAM_KELUAR`, `STATUS_MASUK`, `STATUS_KELUAR`, `KETERANGAN`, `TERLAMBAT`) VALUES
(1, '2025-03-23', '15:26:16', '18:00:00', 'H', NULL, '-', 'Y'),
(2, '2025-03-24', '01:39:58', '18:00:00', 'H', NULL, '-', 'Y'),
(3, '2025-03-24', '01:43:48', '18:00:00', 'I', NULL, '-', 'N'),
(4, '2025-03-24', '01:58:18', '18:00:00', 'H', NULL, '-', 'N'),
(5, '2025-03-25', '10:48:02', '18:00:00', 'H', NULL, '-', 'Y'),
(6, '2025-03-26', '11:53:39', '18:00:00', 'H', NULL, '-', 'Y'),
(7, '2025-03-29', '12:10:26', '18:00:00', 'H', NULL, '-', 'Y'),
(8, '2025-04-16', '12:00:21', '18:00:00', 'H', NULL, '-', 'Y'),
(9, '2025-04-17', '10:03:28', '18:00:00', 'H', NULL, '-', 'Y'),
(10, '2025-04-17', '10:14:39', '18:00:00', 'H', NULL, '-', 'Y'),
(11, '2025-04-17', '17:52:08', '18:00:00', 'H', NULL, '-', 'Y'),
(12, '2025-04-17', '17:55:38', '18:00:00', 'H', NULL, '-', 'Y'),
(13, '2025-04-18', '10:49:31', NULL, 'I', NULL, 'Izin', 'N'),
(14, '2025-04-18', '10:49:35', NULL, 'I', NULL, 'Izin', 'N'),
(15, '2025-04-18', '11:08:14', NULL, 'I', NULL, 'Izin', 'N'),
(16, '2025-04-18', '11:09:55', NULL, 'S', NULL, 'Sakit', 'N'),
(17, '2025-04-18', '21:56:03', NULL, 'I', NULL, 'Izin', 'N'),
(18, '2025-04-19', '07:13:51', '18:00:00', 'H', NULL, 'Hadir', 'N'),
(19, '2025-04-19', '07:17:25', '18:00:00', 'H', NULL, 'Hadir', 'N'),
(20, '2025-04-19', '07:18:38', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(21, '2025-04-19', '07:24:03', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(22, '2025-04-22', '18:25:19', NULL, 'I', NULL, 'Izin', 'N'),
(23, '2025-04-22', '18:27:40', NULL, 'I', NULL, 'Izin', 'N'),
(24, '2025-04-22', '18:32:37', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(25, '2025-04-23', '11:03:11', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(26, '2025-05-23', '11:37:15', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(27, '2025-05-23', '11:37:35', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(28, '2025-05-24', '11:38:12', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(29, '2025-05-24', '11:38:32', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(30, '2025-05-25', '11:39:06', '18:00:00', 'H', NULL, 'Hadir', 'Y'),
(31, '2025-05-25', '11:42:33', '18:00:00', 'H', NULL, 'Hadir', 'Y');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `absensi`
--
ALTER TABLE `absensi`
  ADD PRIMARY KEY (`ID_ABSENSI`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `absensi`
--
ALTER TABLE `absensi`
  MODIFY `ID_ABSENSI` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
