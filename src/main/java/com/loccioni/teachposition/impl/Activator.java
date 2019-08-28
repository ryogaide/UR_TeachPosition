package com.loccioni.teachposition.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.loccioni.teachposition.impl.TeachPositionInstallationNodeService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Hello world activator for the OSGi bundle URCAPS contribution
 *
 */
public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		context.registerService(SwingInstallationNodeService.class, new TeachPositionInstallationNodeService(), null);
//		System.out.println("Activator says Hello World!");
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
//		System.out.println("Activator says Goodbye World!");
	}
}