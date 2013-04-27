package com.alexecollins.taskalicious;

import com.alexecollins.taskalicious.events.TaskAddedEvent;
import com.alexecollins.taskalicious.events.TaskRemovedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
public class TaskaliciousFrame extends JFrame {

	private final EventBus bus = new EventBus();
	private final Tasks tasks = new Tasks(bus);
	private final Image background;
	private final User user = User.named(System.getProperty("user.name"));
	private final Peer me  = Peer.me();
	private final Peers peers = new Peers(bus);
	{
		peers.put(user, me);
	}

	private final World world = new World(peers, user,  bus);

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

			bus.register(this);
			for (Task task : tasks.findTasksByOwner(user)) {
				added(task);
			}

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
								tasks.addTask(Task.of(user, t.getText()));
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

		}
		@Subscribe
		public void taskAdded(TaskAddedEvent e) {
			added(e.getTask());
		}

		public void added(Task task) {
			if (task.getOwner().equals(user)) {
				TaskPanel p = new TaskPanel(task);
				taskTaskPanelMap.put(task,p);
				add(p, getComponentCount() - 1);
				//add(Box.createRigidArea(new Dimension(1, 1)), getComponentCount() - 1);
				revalidate();
				// TODO JScrollBar vertical = scrollPane.getVerticalScrollBar();
				// vertical.setValue(vertical.getMaximum());
				repaint();
			}
		}

		@Subscribe
		public void taskRemoved(TaskRemovedEvent e) {
			Task task = e.getTask();
			if (taskTaskPanelMap.containsKey(task)) {
				TaskPanel p = taskTaskPanelMap.remove(task);
				p.getParent().remove(p);
				revalidate();
				repaint();
			}
		}
	}

	private class TaskPanel extends JPanel implements Task.TaskListener {
		private final JCheckBox box = new JCheckBox();
		private final JTextField text = new JTextField();
		private final JLabel due = new JLabel();

		public TaskPanel(final Task task) {
			setLayout(new BorderLayout());
			setName("taskPanel");
			setMaximumSize(new Dimension(330, 40));
			add(box, BorderLayout.WEST);
			box.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					task.setState(box.isSelected() ? Task.State.COMPLETE : Task.State.PENDING);
				}
			});
			text.setName("task");
			text.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent focusEvent) {
					text.setText(task.toString());
				}

				@Override
				public void focusLost(FocusEvent focusEvent) {
					task.fromString(text.getText());
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
			}});
			add(text);
			add(due, BorderLayout.EAST);
			update(task);
			task.addListener(this);
		}


		@Override
		public void update(Task task) {
			if (task.getOwner().equals(user)) {
				box.setSelected(task.getState() != Task.State.PENDING);
				text.setText(task.getText());
				if (task.getDue() != null) {
					due.setText(TimeUtil.format(task.getDue()));
				}
				due.setVisible(task.getDue() != null);
				due.setName(task.isOverdue() ? "overdue" : "due");
			} else {
				if (this.getParent() != null)
					this.getParent().remove(this);
			}
			TaskaliciousFrame.this.repaint();
		}
	}

	private class StatusBar extends JPanel {
		private StatusBar() throws UnknownHostException {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			JLabel l = new JLabel(user.getName());
			l.setName("grey");
			add(l);
			add(Box.createHorizontalGlue());
			JLabel l2 = new JLabel(me.toString());
			l2.setName("grey");
			add(l2);
			JButton b = new JButton("+");
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								Peer p = Peer.of(JOptionPane.showInputDialog(TaskaliciousFrame.this, "Enter peer, e.g. 192.168.1.70:" + Peer.DEFAULTS_PORT));
								peers.put(world.whoAreYou(p), p);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(TaskaliciousFrame.this, e, "Failed to add peer: " + e, JOptionPane.ERROR_MESSAGE);
							}
						}
					});
				}
			});
			add(b);

		}
	}
}
