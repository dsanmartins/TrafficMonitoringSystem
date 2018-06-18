package se.lnu.trafficmonitoring.environmentimpl;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import se.lnu.trafficmonitoring.data.TMConstants;

/**
 * Panel for configuring and starting the Environment simulation.
 */
public class ManagementPanel extends JPanel {	
	private static final long serialVersionUID = 5340352577074637129L;
	private JSpinner cameraNumSpinner;
	
	private File cameraLauncher = TMConstants.LAUNCHER;
	
	private JLabel launcherPathLabel;
	private JButton startCamerasButton;
	private JButton stopCamerasButton;
	private JButton connectCamerasButton;
	private JButton selectLauncherButton;
	private JButton connectExternalButton;
	
	private EnvironmentImpl environmentImpl;
	
	private List<Process> processes = new ArrayList<Process>();
	int currentId = 0;

	public ManagementPanel(EnvironmentImpl environmentImpl) {
		if (environmentImpl == null) {
			throw new NullPointerException("Parameter \"environmentImpl\" must not be null.");
		}

		this.environmentImpl = environmentImpl;
		
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		
		GridBagConstraints vgc = new GridBagConstraints();
		vgc.anchor = GridBagConstraints.CENTER;
		vgc.fill = GridBagConstraints.BOTH;
		vgc.gridx = GridBagConstraints.NONE;
		vgc.gridy = GridBagConstraints.RELATIVE;
		vgc.insets = new Insets(5, 5, 5, 5);

		GridBagConstraints hgc = new GridBagConstraints();
		hgc.anchor = GridBagConstraints.CENTER;
		hgc.fill = GridBagConstraints.BOTH;
		hgc.gridx = GridBagConstraints.RELATIVE;
		hgc.gridy = GridBagConstraints.NONE;
		hgc.insets = new Insets(5, 5, 5, 5);

		JPanel centerPanel = new JPanel(new GridBagLayout());
		
		JPanel launcherPathPanel = new JPanel(new GridBagLayout());
		launcherPathLabel = new JLabel(cameraLauncher.getName());
		launcherPathPanel.add(launcherPathLabel, hgc);
		
		selectLauncherButton = new JButton("Select Launcher");
		selectLauncherButton.addActionListener(new SelectLauncherListener());

		startCamerasButton = new JButton("Start Processes");
		startCamerasButton.addActionListener(new StartProcessesListener());
		stopCamerasButton = new JButton("Stop Processes");
		stopCamerasButton.setEnabled(false);
		stopCamerasButton.addActionListener(new StopProcessesListener());
		connectCamerasButton = new JButton("Connect Cameras");
		connectCamerasButton.addActionListener(new ConnectCamerasListener());
		connectExternalButton = new JButton("Connect External");
		connectExternalButton.addActionListener(new ConnectExternalListener());

		JPanel cameraNumPanel = new JPanel(new GridBagLayout());
		cameraNumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        cameraNumSpinner.setEditor(new JSpinner.NumberEditor(cameraNumSpinner));
		cameraNumPanel.add(new JLabel("Number of cameras: "), hgc);
        cameraNumPanel.add(cameraNumSpinner, hgc);

        centerPanel.add(launcherPathPanel, vgc);
		centerPanel.add(cameraNumPanel, vgc);
		centerPanel.add(selectLauncherButton, vgc);
		centerPanel.add(startCamerasButton, vgc);
		centerPanel.add(stopCamerasButton, vgc);
		centerPanel.add(connectCamerasButton, vgc);
		
		/* Disabled as it causes problems with organizations. */
		// centerPanel.add(connectExternalButton, vgc);

		add(centerPanel, BorderLayout.CENTER);
	}

	private class StartProcessesListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (!cameraLauncher.isFile() || !cameraLauncher.canRead()) {
				JOptionPane.showMessageDialog(
						ManagementPanel.this,
						"Invalid launcher path!",
						"Invalid launcher path!",
						JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			
			startCamerasButton.setEnabled(false);
			cameraNumSpinner.setEnabled(false);
			selectLauncherButton.setEnabled(false);
			
			int numOfCameras = Integer.parseInt(cameraNumSpinner.getValue().toString());
			
			try {
				Runtime runtime = Runtime.getRuntime(); 
				
				for (int i = 0; i < numOfCameras; i++) {
					Process process = runtime.exec(
							"java -jar " + cameraLauncher.getAbsolutePath() + " " + (TMConstants.BASE_PORT + i),
							new String[] {},
							cameraLauncher.getParentFile()
					);
					
					processes.add(process);
				}
				
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				
				JOptionPane.showMessageDialog(
						ManagementPanel.this,
						sw.toString(),
						"Failed to launch camera!",
						JOptionPane.ERROR_MESSAGE
				);
				
				System.exit(1);
			}
			
			stopCamerasButton.setEnabled(true);
		}
	}

	private class StopProcessesListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			stopCamerasButton.setEnabled(false);
			
			for (Process process : processes) {
				try {
					process.destroy();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			if (connectCamerasButton.isEnabled()) {
				startCamerasButton.setEnabled(true);
				cameraNumSpinner.setEnabled(true);
				selectLauncherButton.setEnabled(true);
			} else {
				connectExternalButton.setEnabled(false);
			}
		}
	}
	
	private class ConnectCamerasListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {			
			startCamerasButton.setEnabled(false);
			selectLauncherButton.setEnabled(false);
			cameraNumSpinner.setEnabled(false);
			connectCamerasButton.setEnabled(false);
			
			int numOfCameras = Integer.parseInt(cameraNumSpinner.getValue().toString());
			
			for (int i = 0; i < numOfCameras; i++) {
				environmentImpl.addCamera(currentId++);
			}
		}
	}
	
	private class ConnectExternalListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String input = JOptionPane.showInputDialog(
					ManagementPanel.this,
					"Enter address:",
					"localhost:" + TMConstants.BASE_PORT
			);
			
			if (input == null) {
				return;
			}
			
			String[] address = input.trim().split(":");
			
			if (address.length != 2) {
				JOptionPane.showMessageDialog(
						ManagementPanel.this,
						"Address must contain a single port number.",
						"Illegal Address",
						JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			
			InetSocketAddress socketAddress;
			
			try {
				socketAddress = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(
						ManagementPanel.this,
						"The provided address was invalid.",
						"Illegal Address",
						JOptionPane.ERROR_MESSAGE
				);
				return;
			}
			
			if (socketAddress.isUnresolved()) {
				JOptionPane.showMessageDialog(
						ManagementPanel.this,
						"Could not resolve the provided address.",
						"Unresolvable Address",
						JOptionPane.ERROR_MESSAGE
				);
				return;
			}

			startCamerasButton.setEnabled(false);
			selectLauncherButton.setEnabled(false);
			cameraNumSpinner.setEnabled(false);
			connectCamerasButton.setEnabled(false);
			
			environmentImpl.addCamera(currentId++, socketAddress);
		}
	}
	
	private class SelectLauncherListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				@Override
				public String getDescription() {
					return "*.jar";
				}
				
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().matches(".*\\.[Jj][Aa][Rr]$");
				}
			});
			
			int returnValue = fileChooser.showOpenDialog(ManagementPanel.this);
			
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				cameraLauncher = fileChooser.getSelectedFile();
				launcherPathLabel.setText(cameraLauncher.getName());
			}
		}
	}
}
