import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import CategoryScreen from './CategoryScreen';
import AccountScreen from './AccountScreen';
import PaymentMethodScreen from './PaymentMethodScreen';
import ViewExpenseScreen from './ViewExpenseScreen';
import { FontAwesome5 } from '@expo/vector-icons';
import Colors from '../config/Colors';
import Size from '../config/Size';
import Icons from './Icons';


const Tab = createBottomTabNavigator();

function HomeScreen(props) {
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
            <Tab.Screen name='Expenses' component={ViewExpenseScreen} />
            <Tab.Screen name='Categories' component={CategoryScreen} />
            <Tab.Screen name='Accounts' component={AccountScreen} />
            <Tab.Screen name='Payments' component={PaymentMethodScreen} />
        </Tab.Navigator>
    );
}

export default HomeScreen;