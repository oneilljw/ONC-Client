package ourneighborschild;

import java.util.EnumSet;

public enum UserStatus 
{
	Any,
	Inactive,
	Reset_PW,
	Update_Profile,
	Recovery,
	Change_PW,
	Active;
	
	static UserStatus[] getStatusValues()
	{
		UserStatus[] usValues = {UserStatus.Inactive, UserStatus.Reset_PW, UserStatus.Update_Profile,
								UserStatus.Recovery, UserStatus.Change_PW, UserStatus.Active};
		return usValues;
	}
	
	public static EnumSet<UserStatus> getActiveUserSet() { return EnumSet.of(UserStatus.Update_Profile,
													UserStatus.Change_PW, UserStatus.Active); }
}
