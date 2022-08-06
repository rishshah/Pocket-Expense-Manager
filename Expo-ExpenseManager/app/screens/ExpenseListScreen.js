import React, { useEffect, useState } from 'react';
import {SectionList, Text, View, StyleSheet, TouchableNativeFeedback, SafeAreaView, StatusBar, Platform } from 'react-native';
import { useIsFocused, useNavigation } from '@react-navigation/native';
import { FontAwesome5 } from '@expo/vector-icons';

import CustomButton from '../components/CustomButton';
import PrimaryNavBar from '../components/PrimaryNavBar';
import { executeSql, Months } from '../components/Sql';
import Colors from '../config/Colors';
import Size from '../config/Size';
import { categoryIcons } from '../config/Icons';


function ExpenseListScreen() {
    const navigation = useNavigation();
    const [expenseList, setExpsenseList] = useState([]);
    
    const getExpenseData = async (tx, results) => {
        var len = results.rows.length;
        var myDataMap = {}
        for (let i=0; i<len; i++) {
            var sqlRow = results.rows.item(i);
            var myRow = {
                id: sqlRow.Id,
                day: sqlRow.Day,
                month: sqlRow.Month,
                title: sqlRow.Title,
                otherInfo: sqlRow.OtherInfo,
                amount: sqlRow.Amount,
                method: sqlRow.Method,
                category: sqlRow.Category,
                isIncome: sqlRow.IsIncome,
            };
            var sectionHeader = `${sqlRow.Month}, ${sqlRow.Year}`;
            if (sectionHeader in myDataMap) {
                myDataMap[sectionHeader].data.push(myRow);
            }
            else {
                myDataMap[sectionHeader] = {
                    title: sectionHeader,
                    data: [myRow],
                };
            } 
        }
        var sortedSections = Object.keys(myDataMap).sort((a, b) => {
            var [monthA, yearA] = a.split(', ');
            var [monthB, yearB] = b.split(', ');
            yearA = parseInt(yearA);
            yearB = parseInt(yearB);
            if (yearA != yearB) {
                return yearB - yearA;
            } else {
                monthA = Months.indexOf(monthA);
                monthB = Months.indexOf(monthB);
                return monthB - monthA;
            }
        });
        var myData = [];
        for (let i=0; i<sortedSections.length; i++) {
            myData.push(myDataMap[sortedSections[i]]);
        }
        setExpsenseList(myData);
    }
    
    const isFocused = useIsFocused();
    useEffect(() => {
        if (isFocused) {
            executeSql(
                `SELECT Id, Day, Month, Year, Title, Amount, Method, Category, OtherInfo, IsIncome FROM Expense ORDER BY Id DESC`,
                getExpenseData
            );
        }
    }, [isFocused]);
    
    const onExpensePressed = (item) => {
        if (item.isIncome == null)
            navigation.navigate('TransferMoney', {pageTitle: 'Modify Transfer', id: item.id});
        else
            navigation.navigate('ViewExpense', {pageTitle: 'Modify Expense', id: item.id});
    }
    return (
        <SafeAreaView style={styles.container}>
            <PrimaryNavBar title='Expenses'/>
            <SectionList
                sections={expenseList}
                renderItem={({ item }) =>(
                    <TouchableNativeFeedback onPress={() => onExpensePressed(item)}>
                        <View style={styles.itemRow}>
                            <View style={[styles.itemCol, {width: '13%'}]}>
                                <Text style={styles.itemMonth}> {item.month.substring(0, 3)}</Text>
                                <Text style={styles.itemDay}> {item.day}</Text>
                            </View>
                            <View style={[styles.itemCol, {width: '10%', alignSelf: 'center'}]}>
                                <FontAwesome5 style={{top: -5}} name={
                                    (item.category in categoryIcons) ? categoryIcons[item.category] : categoryIcons.Other
                                } size={28} color={Colors.primary}/>
                            </View>
                            <View style={[styles.itemCol, {width: '49%'}]}>
                                <Text numberOfLines={1} style={styles.itemTitle}> {item.title}</Text>
                                <Text numberOfLines={1} style={styles.itemOtherInfo}> {item.otherInfo}</Text>
                            </View>
                            <View style={[styles.itemCol, {width: '28%'}]}>
                                <Text numberOfLines={1} style={[
                                    styles.itemMethod,
                                    {color: item.isIncome ? Colors.primary : Colors.secondary}
                                ]}> {item.method ? item.method : 'Transfer'}</Text>
                                <Text numberOfLines={1} style={[
                                    styles.itemAmount,
                                    {color: item.isIncome ? Colors.primary : Colors.secondary}
                                ]}> {item.amount}</Text>
                            </View>
                        </View>
                    </TouchableNativeFeedback>
                )}
                renderSectionHeader={({ section }) =>(
                    <View style={styles.sectionContainer}>
                        <Text style={styles.sectionText}> {section.title}</Text>
                    </View>
                )}
            />
            <View style={styles.buttonContainer}>
                <CustomButton 
                    onPress={() => navigation.navigate('TransferMoney')}
                    title='Transfer Money' bgColor={Colors.primary} fgColor={Colors.white}
                />
                <CustomButton 
                    onPress={() => navigation.navigate('ViewExpense', {pageTitle: 'Create New Expense', id: -1})}
                    title='New Expense' bgColor={Colors.secondary} fgColor={Colors.white}
                />
            </View>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingTop: Platform.OS === 'android' ? StatusBar.currentHeight : 0,
        backgroundColor: Colors.background,
    },
    buttonContainer: {
        position: 'absolute',
        bottom: 25,
        right: 0,
        width: '50%',
    },
    itemRow: {
        height: 50,
        flex: 1,
        flexDirection: 'row',
        borderBottomWidth: 0.1,
    },
    itemCol: {
        flexDirection: 'column',
    },
    itemMonth: {
        paddingRight: 10,
        fontFamily: 'monospace',
        fontWeight: '200',
        alignSelf: 'flex-end',
        fontSize: Size.font_s,
    },
    itemDay: {
        paddingRight: 10,
        fontFamily: 'monospace',
        fontWeight: '800',
        alignSelf: 'flex-end',
        fontSize: Size.font_xl,
    },
    itemTitle: {
        fontSize: Size.font_xl,
        fontFamily: 'sans-serif-condensed',
        fontWeight: '600',
    },
    itemOtherInfo: {
        fontSize: Size.font_s,
        fontFamily: 'sans-serif-condensed',
        fontWeight: '200',
    },
    itemAmount: {
        paddingRight: 20,
        fontFamily: 'monospace',
        fontWeight: '800',
        alignSelf: 'flex-end',
        fontSize: Size.font_xl,
    },
    itemMethod: {
        paddingRight: 20,
        fontFamily: 'monospace',
        fontWeight: '200',
        alignSelf: 'flex-end',
        fontSize: Size.font_s,
    },
    sectionContainer:{
        width: '100%',
        paddingHorizontal: 5,
        paddingVertical: 1,
        borderColor: Colors.secondary,
        borderBottomWidth: 0.5,
    },
    sectionText: {
        fontWeight: '800',
        fontSize: Size.font_l,
        fontFamily: 'Roboto',
        color: Colors.secondary,
    },
})
export default ExpenseListScreen;