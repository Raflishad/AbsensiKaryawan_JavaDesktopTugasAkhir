package absensi;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafis_g7mh0rp
 */
public class adminLaporan extends javax.swing.JFrame {
    private DefaultTableModel model;
    /**
     * Creates new form Admin
     */
    public adminLaporan() {
        initComponents();
        setLocationRelativeTo(null);
        
        jLabel1.setOpaque(true); // Wajib untuk menampilkan warna background
        jLabel1.setBackground(Color.WHITE);
        jTable1.getTableHeader().setReorderingAllowed(false);
        
        // Model untuk tabel
        model = new DefaultTableModel(){
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Semua sel tidak bisa diedit
    }
};
        model.setColumnIdentifiers(new String[]{"NIP", "Nama", "Bagian", "Hadir", "Izin", "Sakit", "Terlambat"});
        jTable1.setModel(model); // Pastikan jTable1 sudah ada di desain GUI

        // Event listener untuk mengaktifkan minggu ketika bulan dipilih
        jComboBox2.addActionListener(e -> updateMinggu());

    applyHoverEffect(panel1);
    }

    public static void applyHoverEffect(Panel panel) {
    Color hoverColor = new Color(0, 102, 102);
    Color hoverTextColor = Color.WHITE; // Warna teks saat hover

    for (Component comp : panel.getComponents()) {
        if (comp instanceof Button) {
            Button button = (Button) comp;
            Color defaultColor = button.getBackground();
            Color defaultTextColor = button.getForeground(); // Simpan warna teks awal

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(hoverColor);
                    button.setForeground(hoverTextColor); // Ubah warna teks jadi putih
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(defaultColor);
                    button.setForeground(defaultTextColor); // Kembalikan warna teks awal
                }
            });
        }
    }
}
    
    
   private void updateMinggu() {
        int bulan = jComboBox2.getSelectedIndex();
        if (bulan == 0) {
            jComboBox1.setEnabled(false);
            jComboBox1.removeAllItems();
            jComboBox1.addItem("Pilih Minggu");
            return;
        }

        // Hitung jumlah hari dalam bulan yang dipilih
        int tahun = LocalDate.now().getYear();
        int jumlahHari = YearMonth.of(tahun, bulan).lengthOfMonth();

        // Reset Minggu
        jComboBox1.removeAllItems();
        jComboBox1.setEnabled(true);

        // Tambahkan pilihan "Semua Hari"
        jComboBox1.addItem("Semua Hari");

        // Tambahkan minggu berdasarkan interval 7 hari
        int start = 1;
        while (start + 6 <= jumlahHari) {
            jComboBox1.addItem(start + " - " + (start + 6));
            start += 7;
        }

        // Jika ada sisa hari, buat minggu ke-5
        if (start <= jumlahHari) {
            jComboBox1.addItem(start + " - " + jumlahHari);
        }
    }
   
public int getBulanIndex(String namaBulan) {
    String[] bulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                      "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
    for (int i = 0; i < bulan.length; i++) {
        if (bulan[i].equalsIgnoreCase(namaBulan.trim())) {
            return i + 1;
        }
    }
    return 0;
}



public void tampilkanDetailTerlambat(String nip) {
    String bulan = jComboBox2.getSelectedItem().toString(); // Ambil bulan dari combo box
    List<String> tanggalTerlambat = new ArrayList<>();
    
    String minggu = jComboBox1.getSelectedItem().toString();
    boolean filterTanggal = false;
    int tanggalMulai = 0;
    int tanggalSelesai = 0;

    if (!minggu.equals("Semua Hari")) {
        String[] tanggalRange = minggu.split(" - ");
        tanggalMulai = Integer.parseInt(tanggalRange[0]);
        tanggalSelesai = Integer.parseInt(tanggalRange[1]);
        filterTanggal = true;
    }

    try {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/absensi", "root", "");
        String sql = """
             SELECT a.tanggal_absen 
             FROM absensi a
             JOIN rel_absensi r ON a.id_absensi = r.id_absensi
             WHERE UPPER(r.nip_pegawai) = ? 
               AND MONTH(a.tanggal_absen) = ? 
               AND UPPER(TRIM(a.terlambat)) = 'Y'""";

        if (filterTanggal) {
            sql += " AND DAY(a.tanggal_absen) BETWEEN ? AND ?";
        }

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nip.trim().toUpperCase());
        ps.setInt(2, getBulanIndex(bulan)); // convert bulan ke angka
        if (filterTanggal) {
            ps.setInt(3, tanggalMulai);
            ps.setInt(4, tanggalSelesai);
        }
        ResultSet rs = ps.executeQuery();

        
        while (rs.next()) {
            tanggalTerlambat.add(rs.getString("tanggal_absen"));
        }

        if (tanggalTerlambat.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Tidak ada keterlambatan untuk pegawai ini.");
        } else {
            JOptionPane.showMessageDialog(null, "Tanggal terlambat:\n" + String.join("\n", tanggalTerlambat));
        }

        rs.close();
        ps.close();
        conn.close();

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}


    private void tampilkanLaporan() {
    model.setRowCount(0); // Hapus data lama

    int bulan = jComboBox2.getSelectedIndex();
    String minggu = (String) jComboBox1.getSelectedItem();

    if (bulan == 0 || minggu == null || minggu.equals("Pilih Minggu")) {
        JOptionPane.showMessageDialog(this, "Pilih bulan dan minggu terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Query SQL dasar
    String sql = "SELECT p.nip_pegawai, p.nama, b.nama_bagian, " +
            "COUNT(CASE WHEN a.status_masuk = 'H' THEN 1 ELSE NULL END) AS hadir, " +
            "COUNT(CASE WHEN a.status_masuk = 'I' THEN 1 ELSE NULL END) AS izin, " +
            "COUNT(CASE WHEN a.status_masuk = 'S' THEN 1 ELSE NULL END) AS sakit, " +
            "COUNT(CASE WHEN a.terlambat = 'Y' THEN 1 ELSE NULL END) AS terlambat " +
            "FROM pegawai p " +
            "LEFT JOIN bagian b ON b.id_bagian = p.id_bagian " +
            "JOIN rel_absensi r ON p.nip_pegawai = r.nip_pegawai " +
            "JOIN absensi a ON r.id_absensi = a.id_absensi " +
            "WHERE MONTH(a.tanggal_absen) = ? ";

    boolean filterTanggal = false;
    int tanggalMulai = 0;
    int tanggalSelesai = 0;

    // Jika bukan "Semua Hari", filter berdasarkan tanggal
    if (!minggu.equals("Semua Hari")) {
        String[] tanggalRange = minggu.split(" - ");
        tanggalMulai = Integer.parseInt(tanggalRange[0]);
        tanggalSelesai = Integer.parseInt(tanggalRange[1]);
        sql += "AND DAY(a.tanggal_absen) BETWEEN ? AND ? ";
        filterTanggal = true;
    }

    sql += "GROUP BY p.nip_pegawai, p.nama, b.nama_bagian ORDER BY p.nip_pegawai ASC;";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/absensi", "root", "");
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, bulan); // Set parameter bulan
        if (filterTanggal) {
            stmt.setInt(2, tanggalMulai); // Set parameter tanggal mulai
            stmt.setInt(3, tanggalSelesai); // Set parameter tanggal selesai
        }

        ResultSet rs = stmt.executeQuery();

        boolean adaData = false;
        while (rs.next()) {
            adaData = true;
            model.addRow(new Object[]{
                    rs.getString("nip_pegawai"),
                    rs.getString("nama"),
                    rs.getString("nama_bagian"),
                    rs.getInt("hadir"),
                    rs.getInt("izin"),
                    rs.getInt("sakit"),
                    rs.getInt("terlambat")
            });
        }

        // Jika tidak ada data, tampilkan pesan peringatan
        if (!adaData) {
            JOptionPane.showMessageDialog(this, "Tidak ada data absensi!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Gagal mengambil data!", "Error", JOptionPane.ERROR_MESSAGE);
    }
}




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        label1 = new java.awt.Label();
        button1 = new java.awt.Button();
        button2 = new java.awt.Button();
        button3 = new java.awt.Button();
        button7 = new java.awt.Button();
        button5 = new java.awt.Button();
        button4 = new java.awt.Button();
        button6 = new java.awt.Button();
        button8 = new java.awt.Button();
        panel2 = new java.awt.Panel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        button9 = new java.awt.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        panel1.setBackground(new java.awt.Color(0, 102, 102));

        label1.setAlignment(java.awt.Label.CENTER);
        label1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        label1.setForeground(new java.awt.Color(255, 255, 0));
        label1.setText("PT. SUMBER TEKNIK INDONESIA");

        button1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button1.setLabel("HOME");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        button2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button2.setLabel("DATA PEGAWAI");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button2ActionPerformed(evt);
            }
        });

        button3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button3.setLabel("ABSENSI");
        button3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button3ActionPerformed(evt);
            }
        });

        button7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button7.setLabel("LAPORAN");
        button7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button7ActionPerformed(evt);
            }
        });

        button5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button5.setLabel("JABATAN");
        button5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button5ActionPerformed(evt);
            }
        });

        button4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button4.setLabel("BAGIAN\n");
        button4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button4ActionPerformed(evt);
            }
        });

        button6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button6.setLabel("TAMBAH PEGAWAI");
        button6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button6ActionPerformed(evt);
            }
        });

        button8.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        button8.setLabel("LOGOUT");
        button8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(button8, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button5, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button6, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button2, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button7, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button4, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        panel2.setBackground(new java.awt.Color(255, 255, 255));
        panel2.setPreferredSize(new java.awt.Dimension(577, 590));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Data Laporan Pegawai");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Minggu" }));
        jComboBox1.setEnabled(false);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Bulan", "Januari  ", "Februari  ", "Maret  ", "April  ", "Mei  ", "Juni  ", "Juli  ", "Agustus  ", "September  ", "Oktober  ", "November  ", "Desember" }));

        jButton1.setBackground(new java.awt.Color(0, 51, 51));
        jButton1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Tampilkan");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        button9.setLabel("button9");

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 53, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE)
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        admin admin = new admin();
        admin.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button1ActionPerformed

    private void button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button2ActionPerformed
        adminDataPegawai adminDT = new adminDataPegawai();
        adminDT.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button2ActionPerformed

    private void button3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button3ActionPerformed
        adminAbsensi adminA = new adminAbsensi();
        adminA.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button3ActionPerformed

    private void button7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button7ActionPerformed
        adminLaporan laporan = new adminLaporan();
        laporan.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button7ActionPerformed

    private void button5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button5ActionPerformed
        adminJabatan jabatan = new adminJabatan();
        jabatan.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button5ActionPerformed

    private void button4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button4ActionPerformed
        adminBagian bagian = new adminBagian();
        bagian.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button4ActionPerformed

    private void button6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button6ActionPerformed
     adminTambahPegawai tambah = new adminTambahPegawai();
        tambah.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button6ActionPerformed

    private void button8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button8ActionPerformed
      int response = JOptionPane.showConfirmDialog(this, 
        "Apakah Anda yakin ingin logout dari Admin?", 
        "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
    
    if (response == JOptionPane.YES_OPTION) {
        login login = new login();
        login.setVisible(true);
        this.dispose(); //Menutup form saat ini
    }  // TODO add your handling code here:
    }//GEN-LAST:event_button8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       tampilkanLaporan();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        int row = jTable1.getSelectedRow();
        String nip = jTable1.getValueAt(row, 0).toString().trim(); // ambil NIP
        tampilkanDetailTerlambat(nip);
    }//GEN-LAST:event_jTable1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(adminLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(adminLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(adminLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(adminLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new adminLaporan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button button1;
    private java.awt.Button button2;
    private java.awt.Button button3;
    private java.awt.Button button4;
    private java.awt.Button button5;
    private java.awt.Button button6;
    private java.awt.Button button7;
    private java.awt.Button button8;
    private java.awt.Button button9;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private java.awt.Label label1;
    private java.awt.Panel panel1;
    private java.awt.Panel panel2;
    // End of variables declaration//GEN-END:variables
}
