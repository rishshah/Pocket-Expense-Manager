import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { FontAwesome5 } from '@expo/vector-icons';

import Colors from '../config/Colors';
import Size from '../config/Size';
import Icons from '../config/Icons';
import ListScreen from './ListScreen';
import MainScreen from './MainScreen';


const Tab = createBottomTabNavigator();

function HomeScreen() {
    return (
        <Tab.Navigator 
            screenOptions={
                ({ route }) => ({
                tabBarIcon: ({focused, size, color}) => {
                    size = focused ? 24 : 18;
                    color = focused ? Colors.primary : Colors.secondary;
                    return (
                        <FontAwesome5 name={Icons[route.name]} size={size} color={color} />
                    )
                },
                headerShown: false,
                tabBarActiveTintColor: Colors.primary,
                tabBarInactiveTineColor: Colors.secondary,
                tabBarActiveBackgroundColor: Colors.white,
                tabBarInactiveBackgroundColor: Colors.grey,
                tabBarHideOnKeyboard: true,
                tabBarLabelStyle: {
                    fontSize: Size.font_s,
                    fontWeight: '500',
                    marginTop: -5,
                    marginBottom: 5,
                }
            })
               
            }
        >
            <Tab.Screen name='Expenses' component={MainScreen} />
            <Tab.Screen name='Categories' component={ListScreen} initialParams={{ type: 'Category', title: 'Categories' }} />
            <Tab.Screen name='Accounts' component={ListScreen} initialParams={{ type: 'Account', title: 'Accounts' }} />
        </Tab.Navigator>
    );
}

export default HomeScreen;