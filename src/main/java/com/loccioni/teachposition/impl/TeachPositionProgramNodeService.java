package com.loccioni.teachposition.impl;

import java.util.Locale;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.data.DataModel;

public class TeachPositionProgramNodeService implements SwingProgramNodeService<TeachPositionProgramNodeContribution, TeachPositionProgramNodeView>{

	@Override
	public String getId() {
		return "TeachNodeDev";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public String getTitle(Locale locale) {
		return "Teach Positions Dev";
	}

	@Override
	public TeachPositionProgramNodeView createView(ViewAPIProvider apiProvider) {
		return new TeachPositionProgramNodeView(apiProvider);
	}

	@Override
	public TeachPositionProgramNodeContribution createNode(ProgramAPIProvider apiProvider, TeachPositionProgramNodeView view,
			DataModel model, CreationContext context) {
		return new TeachPositionProgramNodeContribution(apiProvider, view, model);
	}

}
