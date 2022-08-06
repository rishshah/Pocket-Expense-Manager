import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import TransferMoneyScreen from './TransferMoneyScreen';
import ExpenseListScreen from './ExpenseListScreen';
import StatisticsScreen from './StatisticsScreen';
import ViewExpenseScreen from './ViewExpenseScreen';


const Stack = createNativeStackNavigator();

function MainScreen() {
    return (
        <Stack.Navigator screenOptions={{headerShown: false}}>
            <Stack.Screen name='Main' component={ExpenseListScreen} />
            <Stack.Screen name='Statistics' component={StatisticsScreen} />
            <Stack.Screen 
                name='ViewExpense' component={ViewExpenseScreen} 
                initialParams={{pageTitle: 'Create New Expense', id: -1}}
            />
            <Stack.Screen 
                name='TransferMoney' component={TransferMoneyScreen}
                initialParams={{pageTitle: 'Create New Transfer', id: -1}}
            />
        </Stack.Navigator>
    );
}

export default MainScreen;