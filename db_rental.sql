CREATE DATABASE db_rental;
USE db_rental;

CREATE TABLE pelanggan (
  idPelanggan VARCHAR(10) PRIMARY KEY,
  nama VARCHAR(100), 
  noHp VARCHAR(15)
);

CREATE TABLE jenis_komputer (
  idJenis VARCHAR(10) PRIMARY KEY,
  namaJenis VARCHAR(50), 
  hargaPerJam INT
);

CREATE TABLE komputer (
  idKomputer VARCHAR(10) PRIMARY KEY,
  merk VARCHAR(100),
  idJenis VARCHAR(10),
  FOREIGN KEY (idJenis) REFERENCES jenis_komputer(idJenis)
);

CREATE TABLE petugas (
  idPetugas VARCHAR(10) PRIMARY KEY,
  namaPetugas VARCHAR(100)
);

CREATE TABLE rental (
  idRental VARCHAR(10) PRIMARY KEY,
  idPelanggan VARCHAR(10), 
  idKomputer VARCHAR(10),
  idPetugas VARCHAR(10), 
  lamaSewa INT,
  FOREIGN KEY (idPelanggan) REFERENCES pelanggan(idPelanggan),
  FOREIGN KEY (idKomputer) REFERENCES komputer(idKomputer),
  FOREIGN KEY (idPetugas) REFERENCES petugas(idPetugas)
);

CREATE TABLE pembayaran (
  idPembayaran VARCHAR(10) PRIMARY KEY,
  idRental VARCHAR(10), 
  bayar INT, 
  kembalian INT,
  FOREIGN KEY (idRental) REFERENCES rental(idRental)
);

