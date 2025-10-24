package absensi;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author rafis_g7mh0rp
 */
public class ownerLaporan extends javax.swing.JFrame {
private DefaultTableModel model;
    /**
     * Creates new form Admin
     */
    public ownerLaporan() {
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

    sql += "GROUP BY p.nip_pegawai, p.nama, b.nama_bagian ORDER BY p.nama;";

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
        button8 = new java.awt.Button();
        panel2 = new java.awt.Panel();
        jLabel1 = new javax.swing.JLabel();
        button4 = new java.awt.Button();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

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
                    .addComponent(button1, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button2, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button3, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(button7, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addComponent(label1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(button1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(button8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(216, Short.MAX_VALUE))
        );

        panel2.setBackground(new java.awt.Color(255, 255, 255));
        panel2.setPreferredSize(new java.awt.Dimension(577, 590));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Data Laporan");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        button4.setLabel("button4");

        jComboBox2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Bulan", "Januari  ", "Februari  ", "Maret  ", "April  ", "Mei  ", "Juni  ", "Juli  ", "Agustus  ", "September  ", "Oktober  ", "November  ", "Desember" }));

        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Semua Minggu" }));
        jComboBox1.setEnabled(false);

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
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
            .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel2Layout.createSequentialGroup()
                    .addGap(34, 34, 34)
                    .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(panel2Layout.createSequentialGroup()
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                            .addComponent(jButton1)))
                    .addGap(35, 35, 35)))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panel2Layout.createSequentialGroup()
                    .addGap(68, 68, 68)
                    .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panel2Layout.createSequentialGroup()
                            .addGap(1, 1, 1)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(18, 18, 18)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(69, 69, 69)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
        owner admin = new owner();
        admin.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button1ActionPerformed

    private void button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button2ActionPerformed
        ownerDataPegawai adminDT = new ownerDataPegawai();
        adminDT.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button2ActionPerformed

    private void button3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button3ActionPerformed
        ownerAbsensi adminA = new ownerAbsensi();
        adminA.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button3ActionPerformed

    private void button7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button7ActionPerformed
        ownerLaporan laporan = new ownerLaporan();
        laporan.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_button7ActionPerformed

    private void button8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button8ActionPerformed
int response = JOptionPane.showConfirmDialog(this, 
        "Apakah Anda yakin ingin logout dari Owner?", 
        "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
    
    if (response == JOptionPane.YES_OPTION) {
        login login = new login();
        login.setVisible(true);
        this.dispose(); //Menutup form saat ini
    }        // TODO add your handling code here:
    }//GEN-LAST:event_button8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        tampilkanLaporan();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(ownerLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ownerLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ownerLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ownerLaporan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ownerLaporan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button button1;
    private java.awt.Button button2;
    private java.awt.Button button3;
    private java.awt.Button button4;
    private java.awt.Button button7;
    private java.awt.Button button8;
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
