package com.alexecollins.taskalicious;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class TaskaliciousFrame extends JFrame {

	private final Repo repo = new Repo();
	private final Image background;
	private final String user = System.getProperty("user.name");

	public TaskaliciousFrame() throws IOException {
		setLayout(new BorderLayout());
		setTitle("Taskalicious");
		this.background = ImageIO.read(getClass().getResource("background.png"));
		setBackground(new Color(0, 0, 0, 0));
		setUndecorated(true);
		setMinimumSize(new Dimension(345, 519));
		ContentPanel contentPanel = new ContentPanel();
		add(contentPanel);
	}

	public class ContentPanel extends JPanel {
		public ContentPanel() throws IOException {
			setLayout(new BorderLayout());
			setBackground(new Color(0, 0, 0, 0));
			setOpaque(true);
			setName("page");
			add(new PagePanel());
			add(new StatusBar(), BorderLayout.SOUTH);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			graphics.drawImage(background, 0, 0, null);
		}

	}

	private class PagePanel extends JPanel implements Repo.RepoListener {
		private final JPanel tasks = new JPanel();
		private final JScrollPane scrollPane = new JScrollPane(tasks);

		PagePanel() {
			setLayout(new BorderLayout());

			tasks.setLayout(new BoxLayout(tasks, BoxLayout.PAGE_AXIS));

			for (Task task : repo.getTasks()) {
				added(repo, task);
			}
			tasks.add(Box.createVerticalGlue());
			scrollPane.setBackground(new Color(0, 0, 0, 0));
			//scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setName("tasksPanel");
			add(scrollPane);

			final JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			final JTextField t = new JTextField("new task...");
			t.setBackground(new Color(0, 0, 0, 0));
			t.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent focusEvent) {
					if (t.getText().equals("new task...")) {
						t.setText("");
					}
				}
			});
			t.setToolTipText("e.g. submit reviews by tomorrow - alex");
			t.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					if (keyEvent.getKeyCode() == 10) {
						repo.addTask(Task.of(user, t.getText()));
						t.setText("");
					}
				}
			});
			p.add(t, BorderLayout.SOUTH);
			add(p, BorderLayout.SOUTH);

			repo.addListener(this);
		}

		@Override
		public void added(Repo repo, Task task) {
			tasks.add(new TaskPanel(task), tasks.getComponentCount() - 1);
			tasks.add(Box.createRigidArea(new Dimension(1,1)), tasks.getComponentCount() - 1);
			tasks.revalidate();
			JScrollBar vertical = scrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
			repaint();
		}
	}

	private class TaskPanel extends JPanel implements Task.TaskListener {
		private final JCheckBox box = new JCheckBox();
		private final JLabel text = new JLabel();
		private final JLabel due = new JLabel();

		public TaskPanel(final Task task) {
			setLayout(new BorderLayout());
			setName("taskPanel");
			setMaximumSize(new Dimension(330,40));
			add(box, BorderLayout.WEST);
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					task.setState(box.isSelected() ? Task.State.COMPLETE : Task.State.PENDING);
				}
			});
			add(text);
			add(due, BorderLayout.EAST);
			update(task);
			task.addListener(this);
		}

		@Override
		public void update(Task task) {
			box.setSelected(task.getState() != Task.State.PENDING);
			text.setText(task.getText());
			if (task.getDue() != null) {
				due.setText(TimeUtil.format(task.getDue()));
			}
			due.setVisible(task.getDue() != null);
			due.setName(task.isOverdue() ? "overdue" : "due");
			TaskaliciousFrame.this.repaint();
		}
	}

	private class StatusBar extends JPanel {
		private StatusBar() throws UnknownHostException {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(new JLabel(user));
			add(Box.createHorizontalGlue());
			add(new JLabel(InetAddress.getLocalHost().getHostName()));
		}
	}
}
