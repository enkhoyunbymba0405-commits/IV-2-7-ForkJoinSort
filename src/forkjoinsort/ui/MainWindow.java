package forkjoinsort.ui;

import forkjoinsort.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/** Үндсэн цонх: зүүн талд удирдлага, баруун талд рекурсийн мод, доор статистик. */
public class MainWindow extends JFrame {

    private final RunConfig cfg = new RunConfig();
    private RunResult result;

    private final TreePanel tree = new TreePanel();
    private final JTextArea info = new JTextArea(7, 30);
    private final JLabel status = new JLabel("Бэлэн.");
    private final JSlider stepSlider = new JSlider(0, 1, 1);
    private final JLabel legend = new JLabel();
    private final JLabel stepInfo = new JLabel(" ");

    private final JSpinner spSize   = new JSpinner(new SpinnerNumberModel(64, 0, 5_000_000, 1));
    private final JSpinner spThresh = new JSpinner(new SpinnerNumberModel(8, 1, 1_000_000, 1));
    private final JSpinner spPar    = new JSpinner(new SpinnerNumberModel(
            Runtime.getRuntime().availableProcessors(), 1, 256, 1));
    private final JSpinner spSeed   = new JSpinner(new SpinnerNumberModel(42, 0, Integer.MAX_VALUE, 1));
    private final JComboBox<RunConfig.Algorithm> cbAlgo =
            new JComboBox<>(RunConfig.Algorithm.values());
    private final JComboBox<DataGenerator.Pattern> cbPat =
            new JComboBox<>(DataGenerator.Pattern.values());
    private final JTextField tfCustom = new JTextField();
    private final JCheckBox cbStep = new JCheckBox("Алхамчилсан горим");

    public MainWindow() {
        super("IV-2-7 · Fork-Join Sorting Application — рекурсийн мод");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildControls(), BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(new TitledBorder("Рекурсийн мод (чирж зөөнө · дугуйгаар томруулна · зангилаа дээр дарна)"));
        center.add(tree, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 0));
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.add(new JLabel("Алхам:"), BorderLayout.WEST);
        row.add(stepSlider, BorderLayout.CENTER);
        south.add(row, BorderLayout.NORTH);
        stepInfo.setFont(new Font("SansSerif", Font.BOLD, 12));
        stepInfo.setBorder(BorderFactory.createEmptyBorder(2, 6, 4, 6));
        south.add(stepInfo, BorderLayout.SOUTH);
        center.add(south, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new TitledBorder("Үр дүн ба сонгосон зангилаа"));
        info.setEditable(false);
        info.setFont(new Font("Monospaced", Font.PLAIN, 12));
        info.setLineWrap(true); info.setWrapStyleWord(true);
        right.add(new JScrollPane(info), BorderLayout.CENTER);
        legend.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        right.add(legend, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(360, 100));
        add(right, BorderLayout.EAST);

        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        tree.setOnSelect(this::showNode);
        stepSlider.addChangeListener(e -> {
            tree.setVisibleCount(stepSlider.getValue());
            stepInfo.setText(cbStep.isSelected() ? tree.stepLabel(stepSlider.getValue()) : " ");
        });
        stepSlider.setEnabled(false);

        setSize(1280, 800);
        setLocationRelativeTo(null);
    }

    private JComponent buildControls() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder("Тохиргоо"));
        p.setPreferredSize(new Dimension(300, 100));

        p.add(row("Алгоритм", cbAlgo));
        p.add(row("Өгөгдлийн хэв маяг", cbPat));
        p.add(row("Массивын хэмжээ n", spSize));
        p.add(row("Threshold", spThresh));
        p.add(row("Урсгалын тоо", spPar));
        p.add(row("Seed", spSeed));

        JPanel cu = new JPanel(new BorderLayout(4, 2));
        cu.setBorder(new TitledBorder("Өөрийн оролт (сонголтоор)"));
        tfCustom.setToolTipText("Жишээ: 7, 12, 19, 3, 18, 4, 2, 6, 15, 8 — хоосон бол дээрх тохиргоог ашиглана");
        cu.add(tfCustom, BorderLayout.CENTER);
        JButton bBook = new JButton("Номын жишээ (Figure 4.27)");
        bBook.addActionListener(e -> tfCustom.setText("7, 12, 19, 3, 18, 4, 2, 6, 15, 8"));
        cu.add(bBook, BorderLayout.SOUTH);
        p.add(cu);

        p.add(cbStep);

        JPanel btns = new JPanel(new GridLayout(0, 1, 0, 4));
        JButton bRun = new JButton("Ажиллуулах");
        JButton bFit = new JButton("Модыг багтаах");
        JButton bReset = new JButton("Анхны тохиргоо (Reset)");
        JButton bSave = new JButton("Модыг PNG болгож хадгалах");
        bRun.addActionListener(e -> doRun());
        bFit.addActionListener(e -> tree.fit());
        bReset.addActionListener(e -> doReset());
        bSave.addActionListener(e -> doSave());
        btns.add(bRun); btns.add(bFit); btns.add(bSave); btns.add(bReset);
        p.add(btns);
        p.add(Box.createVerticalGlue());
        return new JScrollPane(p);
    }

    private JPanel row(String label, JComponent c) {
        JPanel r = new JPanel(new BorderLayout(6, 2));
        r.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        r.add(new JLabel(label), BorderLayout.WEST);
        r.add(c, BorderLayout.CENTER);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return r;
    }

    private void doReset() {
        spSize.setValue(64); spThresh.setValue(8);
        spPar.setValue(Runtime.getRuntime().availableProcessors());
        spSeed.setValue(42);
        cbAlgo.setSelectedIndex(0); cbPat.setSelectedIndex(0);
        tfCustom.setText(""); cbStep.setSelected(false);
        tree.setNodes(null); info.setText(""); legend.setText("");
        stepSlider.setEnabled(false); stepInfo.setText(" ");
        status.setText("Анхны тохиргоонд буцлаа.");
    }

    private void doRun() {
        try {
            cfg.size        = (Integer) spSize.getValue();
            cfg.threshold   = (Integer) spThresh.getValue();
            cfg.parallelism = (Integer) spPar.getValue();
            cfg.seed        = ((Integer) spSeed.getValue()).longValue();
            cfg.algorithm   = (RunConfig.Algorithm) cbAlgo.getSelectedItem();
            cfg.pattern     = (DataGenerator.Pattern) cbPat.getSelectedItem();
            cfg.trace       = true;
            String custom = tfCustom.getText().trim();
            cfg.customInput = custom.isEmpty() ? null : DataGenerator.parse(custom);

            status.setText("Ажиллаж байна…");
            result = SortRunner.run(cfg.copy());

            tree.setNodes(result.nodes);
            int total = Math.max(1, tree.totalSteps());
            stepSlider.setMinimum(0);
            stepSlider.setMaximum(total);
            stepSlider.setValue(cbStep.isSelected() ? 0 : total);
            stepSlider.setEnabled(cbStep.isSelected());
            stepInfo.setText(cbStep.isSelected() ? tree.stepLabel(stepSlider.getValue()) : " ");
            tree.setVisibleCount(stepSlider.getValue());

            info.setText(summary(result));
            legend.setText(legendHtml());
            status.setText(result.correct
                ? "Дууслаа — үр дүн ЗӨВ (Arrays.sort-той тулгасан)."
                : "АНХААР: үр дүн БУРУУ!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Оролтын алдаа", JOptionPane.WARNING_MESSAGE);
            status.setText("Алдаа: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, String.valueOf(ex),
                    "Гэнэтийн алдаа", JOptionPane.ERROR_MESSAGE);
            status.setText("Алдаа: " + ex);
        }
    }

    private void doSave() {
        if (result == null) { status.setText("Эхлээд ажиллуулна уу."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("recursion_tree.png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            javax.imageio.ImageIO.write(tree.toImage(1600, 900), "png", fc.getSelectedFile());
            status.setText("Хадгаллаа: " + fc.getSelectedFile());
        } catch (Exception ex) {
            status.setText("Хадгалж чадсангүй: " + ex);
        }
    }

    public static String summary(RunResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Алгоритм      : ").append(r.config.algorithm).append('\n');
        sb.append("Элементийн тоо: ").append(r.original.length).append('\n');
        sb.append("Threshold     : ").append(r.config.threshold).append('\n');
        sb.append("Урсгал        : ").append(r.config.parallelism).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Зангилаа      : ").append(r.nodeCount).append('\n');
        sb.append("Навч          : ").append(r.leafCount).append('\n');
        sb.append("Модны гүн     : ").append(r.maxDepth).append('\n');
        sb.append("--------------------------------\n");
        sb.append("Fork-join     : ").append(RunResult.ms(r.parallelNanos)).append('\n');
        sb.append("Нэг урсгалт   : ").append(RunResult.ms(r.sequentialNanos)).append('\n');
        sb.append(String.format("Хурдасгалт    : %.2f×%n", r.speedup()));
        sb.append("Зөв эсэх      : ").append(r.correct ? "ЗӨВ" : "БУРУУ").append('\n');
        sb.append("--------------------------------\n");
        sb.append("Урсгал тус бүрийн зангилаа:\n");
        r.perThread.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> sb.append("  ").append(e.getKey()).append(" : ").append(e.getValue()).append('\n'));
        if (r.original.length <= 40) {
            sb.append("--------------------------------\n");
            sb.append("Өмнө : ").append(java.util.Arrays.toString(r.original)).append('\n');
            sb.append("Дараа: ").append(java.util.Arrays.toString(r.sorted)).append('\n');
        }
        return sb.toString();
    }

    private String legendHtml() {
        StringBuilder sb = new StringBuilder("<html><b>Урсгалын өнгө:</b><br>");
        for (Map.Entry<String, Color> e : tree.threadColors().entrySet()) {
            Color c = e.getValue();
            sb.append(String.format("<font color='#%02x%02x%02x'>■</font> %s<br>",
                    c.getRed(), c.getGreen(), c.getBlue(), e.getKey()));
        }
        sb.append("<br>Дүүрэн = навч · цайвар = дотоод"
                + "<br><b>Алхамчилсан горимд:</b>"
                + "<br>цагаан дотортой = FORK хийгдсэн, JOIN хүлээж байна"
                + "<br>будсан = JOIN дууссан</html>");
        return sb.toString();
    }

    private void showNode(TraceNode n) {
        StringBuilder sb = new StringBuilder();
        sb.append("ЗАНГИЛАА #").append(n.id).append('\n');
        sb.append("Муж       : [").append(n.lo).append(", ").append(n.hi).append(")\n");
        sb.append("Хэмжээ    : ").append(n.size()).append('\n');
        sb.append("Гүн       : ").append(n.depth).append('\n');
        sb.append("Төрөл     : ").append(n.leaf ? "НАВЧ (threshold хүрсэн)" : "дотоод (хуваасан)").append('\n');
        sb.append("Урсгал    : ").append(n.thread).append('\n');
        sb.append(String.format("Эхэлсэн   : %.3f мс%n", n.forkNanos / 1e6));
        sb.append(String.format("Дууссан   : %.3f мс%n", n.doneNanos / 1e6));
        if (n.before != null) sb.append("\nӨмнө : ").append(n.before).append('\n');
        if (n.after  != null) sb.append("Дараа: ").append(n.after).append('\n');
        if (n.before == null) sb.append("\n(муж хэтэрхий том тул утгуудыг хадгалаагүй)\n");
        info.setText(sb.toString());
        info.setCaretPosition(0);
    }
}
