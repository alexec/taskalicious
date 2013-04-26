package com.alexecollins.taskalicious;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class TaskaliciousFrame extends JFrame implements Repo.RepoListener {

	private final Repo repo = new Repo();
	private final Image background;
	private final String user = System.getProperty("user.name");
	private final TaskaliciousFrame.ContentPanel contentPanel;

	public TaskaliciousFrame() throws IOException {
		this.background = ImageIO.read(getClass().getResource("background.png"));
		setLayout(new BorderLayout());
		setBackground(new Color(0, 0, 0, 0));
		setUndecorated(true);
		setMinimumSize(new Dimension(345, 519));
		contentPanel = new ContentPanel();
		add(contentPanel);
		repo.addListener(this);
	}

	@Override
	public void added(Repo repo, Task task) {
		contentPanel.add(new TaskPanel(task));
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
			graphics.drawImage(background, 0, 0, null) ;
		}

	}

	private class PagePanel extends JPanel {
		 PagePanel() {
			 //setLayout(new GridLayout(21,1));
			 setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setOpaque(false);

			 for (Task task : repo.getTasks()) {
				 add(new TaskPanel(task));
			 }

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
					 if (keyEvent.getKeyCode() == 13) {
						 repo.addTask(Task.of(user, t.getText()));
					 }
				 }
			 });
			 p.add(t);
			 add(p);

			 add(Box.createVerticalGlue());
		 }
	}

	private class TaskPanel extends JPanel implements Task.TaskListener {
		private final JLabel label = new JLabel();
		private final JCheckBox 			box = new JCheckBox();
		private final Task task;

		public TaskPanel(Task task) {
			this.task = task;
			setLayout(new BorderLayout());
			box.setBackground(new Color(0, 0, 0, 0));
			add(box, BorderLayout.WEST);
			add(label);
			task.addListener(this);
			update(task);
		}

		@Override
		public void update(Task task) {
			box.setSelected(task.getState() != Task.State.PENDING);
			label.setText(task.getText());
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
