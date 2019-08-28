package com.loccioni.teachposition.impl;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.util.Locale;

public class TeachPositionInstallationNodeService 
	implements SwingInstallationNodeService<TeachPositionInstallationNodeContribution, TeachPositionInstallationNodeView> {

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
	}

	@Override
	public String getTitle(Locale locale) {
		return "Teach Position";
	}

	@Override
	public TeachPositionInstallationNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new TeachPositionInstallationNodeView(style);
	}

	@Override
	public TeachPositionInstallationNodeContribution createInstallationNode(InstallationAPIProvider apiProvider, 
			TeachPositionInstallationNodeView view, DataModel model, CreationContext context) {
		return new TeachPositionInstallationNodeContribution(apiProvider, model, view);
	}
}