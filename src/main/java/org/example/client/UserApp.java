package org.example.client;

import org.example.client.models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class UserApp extends JFrame {

    private User loggedInUser; // current user
    private JComboBox<String> sortBox = new JComboBox<>(new String[]{"Sort by", "Price", "Brand"});
    private JPanel productGallery = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));

    private List<Product> allProducts = new ArrayList<>();
    private List<Brand> allBrands = new ArrayList<>();

    public UserApp(User user) {
        this.loggedInUser = user;
        setTitle("🛍️ Shop - " + user.getUsername());
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        getContentPane().setBackground(new Color(245,247,250));

        // Top panel with sort, refresh, cart
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));

        JLabel title = new JLabel("🛒 Product Gallery");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JButton refreshBtn = new JButton("⟳ Refresh");
        refreshBtn.addActionListener(e -> loadProducts());

        JButton cartBtn = new JButton("🛍 View Cart");
        cartBtn.setBackground(new Color(52,152,219));
        cartBtn.setForeground(Color.WHITE);
        cartBtn.addActionListener(e -> new CartPage(loggedInUser.getUserId()).setVisible(true));

        topPanel.add(title);
        topPanel.add(Box.createHorizontalStrut(30));
        topPanel.add(sortBox);
        topPanel.add(refreshBtn);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(cartBtn);

        add(topPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(productGallery);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        sortBox.addActionListener(e -> refreshGallery());

        loadProducts();
    }

    private void loadProducts() {
        try {
            allProducts = ApiClient.fetchProducts();
            allBrands = ApiClient.fetchBrands();
            refreshGallery();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading data: " + e.getMessage());
        }
    }

    private void refreshGallery() {
        productGallery.removeAll();
        for (Product p : allProducts) {
            JPanel card = createProductCard(p);
            productGallery.add(card);
        }
        productGallery.revalidate();
        productGallery.repaint();
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(180,200));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(220,220,220),1,true));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel("<html><center><b>" + p.getName() + "</b></center></html>", SwingConstants.CENTER);
        JLabel priceLabel = new JLabel("₹" + p.getPrice(), SwingConstants.CENTER);
        priceLabel.setForeground(new Color(46,204,113));

        card.add(nameLabel, BorderLayout.CENTER);
        card.add(priceLabel, BorderLayout.SOUTH);

        // Open ProductDetailPage on click
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Brand brand = allBrands.stream()
                        .filter(b -> b.getId().equals(p.getBrandId()))
                        .findFirst()
                        .orElse(new Brand());
                SwingUtilities.invokeLater(() -> {
                    ProductDetailPage detail = new ProductDetailPage(p, brand.getName(), loggedInUser.getUserId());
                    detail.setVisible(true);
                });
            }

            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(240, 245, 255)); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    // ---------- WrapLayout for gallery ----------
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align,hgap,vgap); }
        @Override
        public Dimension preferredLayoutSize(Container target) {
            synchronized(target.getTreeLock()) {
                int maxWidth = target.getParent() != null ? target.getParent().getWidth() : 800;
                int x=0,y=0,rowHeight=0;
                for(Component c : target.getComponents()) {
                    Dimension d = c.getPreferredSize();
                    if(x+d.width>maxWidth){ x=0; y+=rowHeight+getVgap(); rowHeight=0; }
                    x+=d.width+getHgap();
                    rowHeight = Math.max(rowHeight,d.height);
                }
                y+=rowHeight+getVgap();
                Insets insets = target.getInsets();
                y+=insets.top+insets.bottom;
                return new Dimension(maxWidth,y);
            }
        }
    }
}
