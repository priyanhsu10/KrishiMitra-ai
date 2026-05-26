import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface FarmerState {
  id: string | null;
  name: string | null;
  mobile: string | null;
  language: string;
  authToken: string | null;
  isAuthenticated: boolean;
}

const initialState: FarmerState = {
  id: null,
  name: null,
  mobile: null,
  language: 'marathi',
  authToken: null,
  isAuthenticated: false,
};

const farmerSlice = createSlice({
  name: 'farmer',
  initialState,
  reducers: {
    setFarmer: (state, action: PayloadAction<Partial<FarmerState>>) => {
      return { ...state, ...action.payload, isAuthenticated: true };
    },
    logout: () => initialState,
  },
});

export const { setFarmer, logout } = farmerSlice.actions;
export default farmerSlice.reducer;
