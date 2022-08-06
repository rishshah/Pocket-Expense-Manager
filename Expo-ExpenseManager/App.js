import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import WelcomeScreen from './app/screens/WelcomeScreen';
import HomeScreen from './app/screens/HomeScreen';


const Stack = createNativeStackNavigator();
const THEME_COLOR = '#285E29';
export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{headerShown: false}}>
        <Stack.Screen name='Welcome' component={WelcomeScreen} />
        <Stack.Screen name='Home' component={HomeScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

