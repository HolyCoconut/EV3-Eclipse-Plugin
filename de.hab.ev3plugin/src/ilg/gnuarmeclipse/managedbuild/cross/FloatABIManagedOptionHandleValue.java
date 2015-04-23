package ilg.gnuarmeclipse.managedbuild.cross;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;

public class FloatABIManagedOptionHandleValue extends ManagedOptionValueHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue
	 * (IConfiguration,IToolChain,IOption,String,int)
	 */
	@SuppressWarnings("unused")
	public boolean handleValue(IBuildObject configuration,
			IHoldsOptions holder, IOption option, String extraArgument,
			int event) {

		// ManagedOptionValueHandlerDebug.dump(configuration, holder, option,
		// extraArgument, event);

		if (event == EVENT_APPLY) {
			if (configuration instanceof FolderInfo) {
				FolderInfo oFolderInfo;
				oFolderInfo = (FolderInfo) configuration;

				IConfiguration config = ((FolderInfo) configuration)
						.getParent();
				IToolChain toolchain = config.getToolChain();

				IOption opt;
				String val;
				try {
					opt = toolchain
							.getOptionBySuperClassId(Option.OPTION_ARM_TARGET_FAMILY);
					val = opt.getStringValue();
					if (!val.endsWith(Option.OPTION_ARM_MCPU_CORTEXM4))
						return false;

					opt = toolchain
							.getOptionBySuperClassId(Option.OPTION_ARM_TARGET_FLOAT_ABI);
					val = opt.getStringValue();
					if (!val.endsWith(Option.OPTION_ARM_FPU_ABI_HARD)) {
						val = opt.getStringValue();
						if (!val.endsWith(Option.OPTION_ARM_FPU_ABI_SOFTFP)) {

							return false; // neither hard nor softfp;
						}
					}
					opt = toolchain
							.getOptionBySuperClassId(Option.OPTION_ARM_TARGET_FLOAT_UNIT);
					//opt.setValue(Option.OPTION_ARM_FPU_UNIT_FPV4SPD16);
					config.setOption(toolchain, opt, Option.OPTION_ARM_FPU_UNIT_FPV4SPD16);

					return true;
				} catch (BuildException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false; // should we return true?
			}
		}

		return false;
	}
}
