package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.*;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Slf4j
public class TaskaliciousFrame extends JFrame {

	private final EventBus bus = new AsyncEventBus(Executors.newSingleThreadExecutor());
	private final Tasks tasks = new Tasks(bus);
	private final Image background;
	private final User user = User.named(System.getProperty("user", System.getProperty("user.name")));
	private final Peer me  = Peer.me();
	private final Peers peers = new Peers(bus);
	{
		peers.put(user, me);
	}

	private final World world = new World(peers, user,  bus);
	private final TrayIcon trayIcon;

	public TaskaliciousFrame() throws IOException, AWTException {
		setLayout(new BorderLayout());
		setTitle("Taskalicious");
		this.background = ImageIO.read(getClass().getResource("background.png"));
		setBackground(new Color(0, 0, 0, 0));
		setUndecorated(true);
		setMinimumSize(new Dimension(345, 519));
		ContentPanel contentPanel = new ContentPanel();
		add(contentPanel);
		SystemTray tray = SystemTray.getSystemTray();
		// load an image
		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("trayIcon.png"), getTitle(), new PopupMenu());
		tray.add(trayIcon);
		bus.register(this);
		world.start();
	}

	public class ContentPanel extends JPanel {
		public ContentPanel() throws IOException {
			setLayout(new BorderLayout());
			setBackground(new Color(0, 0, 0, 0));
			setOpaque(true);
			setName("page");
			JScrollPane scrollPane = new JScrollPane(new TasksPanel());
			scrollPane.setBackground(new Color(0, 0, 0, 0));
			scrollPane.setName("tasksPanel");
			add(scrollPane);
			add(new StatusBar(), BorderLayout.SOUTH);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			graphics.drawImage(background, 0, 0, null);
		}
	}

	private class TasksPanel extends JPanel {
		final Map<Task,TaskPanel> taskTaskPanelMap = new HashMap<Task, TaskPanel>();

		TasksPanel() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			final JTextField t = new JTextField("new task...");
			t.setMaximumSize(new Dimension(330, 40));
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
					// 38 = up
					// 40 = down
					// 8 = delete
					switch (keyEvent.getKeyCode()) {
						case 10:
							if (t.getText().length() > 0) {
								try {
									tasks.add(Task.of("- " + t.getText() + (t.getText().matches(" - [^-]$") ? "" : " - " + user)));
								} catch (Exception e) {
									bus.post(e);
								}
								t.setText("");
							}
							break;
						case 40:
							//down();
							break;
					}
				}
			});
			add(t);
			bus.register(this);
		}

		@Subscribe
		public void taskAdded(TaskAddedEvent e) {
			Task task = e.getTask();
			final TaskPanel p = new TaskPanel(task);
			taskTaskPanelMap.put(task,p);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					add(p, getComponentCount() - 1);
					//add(Box.createRigidArea(new Dimension(1, 1)), getComponentCount() - 1);
					revalidate();
					// TODO JScrollBar vertical = scrollPane.getVerticalScrollBar();
					// vertical.setValue(vertical.getMaximum());
					//repaint();
				}
			});
		}

		@Subscribe
		public void taskRemoved(TaskRemovedEvent e) {
			Task task = e.getTask();
			if (taskTaskPanelMap.containsKey(task)) {
				final TaskPanel p = taskTaskPanelMap.remove(task);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						p.getParent().remove(p);
						revalidate();
						repaint();
					}
				});
			}
		}
	}

	private class TaskPanel extends JPanel  {
		private final JCheckBox box = new JCheckBox();
		private final JTextField text = new JTextField();
		private final JLabel due = new JLabel();
		private final Task task;

		public TaskPanel(final Task task) {
			this.task = task;
			setLayout(new BorderLayout());
			setName("taskPanel");
			setMaximumSize(new Dimension(330, 40));
			add(box, BorderLayout.WEST);
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					task.setState(box.isSelected() ? Task.State.COMPLETE : Task.State.PENDING);
					bus.post(new TaskChanged(task));
				}
			});
			text.setName("task");
			text.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent focusEvent) {
					text.setText(task.toString().substring(2));
				}

				@Override
				public void focusLost(FocusEvent focusEvent) {
					task.fromString("- " + text.getText());
					bus.post(new TaskChanged(task));
				}
			});
			text.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent keyEvent) {
					// 38 = up
					// 40 = down
					// 8 = delete
					switch (keyEvent.getKeyCode()) {
						case 8:
							if (text.getText().length() == 0) {
								tasks.remove(task);
							}
							break;
						case 38:
							//up();
							break;
						case 10:
						case 40:
							//down();
							break;
					}
				}
			});
			add(text);
			add(due, BorderLayout.EAST);
			bus.register(this);
			fromTask() ;
		}

		private void fromTask() {
			box.setSelected(task.getState() != Task.State.PENDING);
			text.setText(task.getText() + (!task.getOwner().equals(user) ? " - " + task.getOwner() :""));
			if (task.getDue() != null) {
				due.setText(TimeUtils.format(task.getDue()));
			}
			due.setVisible(task.getDue() != null);
			due.setName(task.isOverdue() ? "overdue" : "due");
		}

		@Subscribe
		public void taskChanged(final TaskChanged e) {
			if (e.getTask().equals(task)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						fromTask();
						repaint();
					}
				});
			}
		}
	}

	private class StatusBar extends JPanel {
		private StatusBar() throws UnknownHostException {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			JLabel l = new JLabel(user+ "@" + me);
			l.setName("grey");
			add(l);
			add(Box.createHorizontalGlue());
			JButton b = new JButton("+");
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								Peer p = Peer.of(JOptionPane.showInputDialog(TaskaliciousFrame.this, "Enter peer, e.g. 192.168.1.70", "localhost:" + (Peer.DEFAULTS_PORT + 1)));
								peers.put(world.whoAreYou(p), p);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(TaskaliciousFrame.this, "Failed to add peer: " + e, "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
				}
			});
			add(b);

		}
	}

	@Subscribe
	public void exception(Exception e) {
		log.error("uncaught", e);
		trayIcon.displayMessage("Error", e.toString(), TrayIcon.MessageType.ERROR);
	}

	@Subscribe
	public void peerDiscovered(PeerDiscovered e) {
		if (!e.getPeer().equals(me))
			trayIcon.displayMessage("New peer", e.getUser() + "@" + e.getPeer() + " discovered", TrayIcon.MessageType.INFO);
	}

	@Subscribe
	public void taskDiscovered(TaskDiscovered e) {
		if (e.getTask().getOwner().equals(user)) {
			trayIcon.displayMessage("New task", e.getTask() + " discovered", TrayIcon.MessageType.INFO);
		}
	}

	@Subscribe
	public void peerUnavailable(PeerUnavailable e) {
		trayIcon.displayMessage("Peer unavailable", e.getPeer() + " not available", TrayIcon.MessageType.WARNING);
	}
}
