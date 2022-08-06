import { DateTimePickerAndroid } from '@react-native-community/datetimepicker';
import { onOpen, onClose, Picker } from 'react-native-actions-sheet-picker';
import * as DocumentPicker from 'expo-document-picker';

import { useIsFocused, useNavigation } from '@react-navigation/native';
import React, { useEffect, useState } from 'react';
import { View, StyleSheet, Text, Pressable, Alert, ToastAndroid, SafeAreaView, StatusBar, Platform } from 'react-native';

import CustomButton from '../components/CustomButton';
import CustomInput from '../components/CustomInput';
import { executeSql, Months } from '../components/Sql';
import Colors from '../config/Colors';
import Size from '../config/Size';
import CustomButtonIcon from '../components/CustomButtonIcon';
import Icons from '../config/Icons';


function ViewExpenseScreen({ route }) {
    const {pageTitle, id} = route.params;
    
    const [categoryData, setCategoryData] = useState([]);
    const [methodData, setMethodData] = useState([]);

    const [title, setTitle] = useState('');
    const [amount, setAmount] = useState('');
    const [otherInfo, setOtherInfo] = useState('');
    const [category, setCategory] = useState('Category');
    const [method, setMethod] = useState('PayMethod');
    const [date, setDate] = useState(new Date());
    const [files, setFiles] = useState([]);
    const [expenseType, setExpenseType] = useState('Type:  EXPENSE');

    const navigation = useNavigation();
    const isFocused = useIsFocused();
    useEffect(() => {
        if (isFocused) {
            executeSql(
                'SELECT Name FROM Category;',
                (tx, results) => {
                    var len = results.rows.length;
                    var data = []
                    for (let i=0; i<len; i++) {
                        data.push(results.rows.item(i).Name);
                    }
                    setCategoryData(data);
                }
            );
            executeSql(
                'SELECT Name FROM Method;',
                (tx, results) => {
                    var len = results.rows.length;
                    var data = []
                    for (let i=0; i<len; i++) {
                        data.push(results.rows.item(i).Name);
                    }
                    setMethodData(data);
                }
            );
            if (id >= 0) {
                executeSql(
                    `SELECT * FROM Expense WHERE Id=${id};`,
                    (tx, results) => {
                        var len = results.rows.length;
                        if (len == 1) {
                            var sqlRow = results.rows.item(0);
                            setTitle(sqlRow.Title);
                            setAmount(sqlRow.Amount.toString());
                            setOtherInfo(sqlRow.OtherInfo);
                            setCategory(sqlRow.Category);
                            setMethod(sqlRow.Method);
                            setDate(new Date(sqlRow.Year, Months.indexOf(sqlRow.Month), sqlRow.Day));
                            setExpenseType(sqlRow.IsIncome ? 'Type:  INCOME' : 'Type:  EXPENSE');
                        } else {
                            console.log('Expense Viewing Error');
                        }
                    }
                );
            }
        }
    }, [isFocused]);

    const renderCategoryItem = (item) => {
        return (
            <Pressable style={styles.itemContainer} onPress={() => {
                setCategory(item);
                onClose('category');
            }}>
                <Text style={styles.item}> {item} </Text>
            </Pressable>
        );
    }
    const renderMethodItem = (item) => {
        return (
            <Pressable style={styles.itemContainer} onPress={() => {
                setMethod(item);
                onClose('method');
            }}>
                <Text style={styles.item}> {item} </Text>
            </Pressable>
        );
    }

    
    const onChange = (event, selectedDate) => {
        const currentDate = selectedDate;
        setDate(currentDate);
    };
    const showDatepicker = () => {
        DateTimePickerAndroid.open({
            value: date, onChange, mode: 'date',
        });
    }

    const showFilePicker = async () => {
        let result = await DocumentPicker.getDocumentAsync({});
        console.log(result.uri);
        console.log(result);
    }
    
    const onExpsenseTypePressed = () => {
        if (expenseType.includes('INCOME')) {
            setExpenseType(expenseType.replace('INCOME', 'EXPENSE'));
        } else {
            setExpenseType(expenseType.replace('EXPENSE', 'INCOME'));
        }
    }
    const onConfirmPressed = () => {
        if (amount.trim().length == 0 || isNaN(amount)) {
            Alert.alert('Warning!', `Amount ${amount} invalid!`);
        } else if (category === 'Category' || method === 'PayMethod') {
            Alert.alert('Warning!', `Category/PayMethod invalid!`);
        } else if (title.trim().length === 0){
            Alert.alert('Warning!', `Description Empty!`);
        } else {
            let sql;
            if (id > 0) {
                sql = `UPDATE Expense SET Day = ${date.getDate()},
                    Month = '${Months[date.getMonth()]}', Year = ${date.getFullYear()},
                    Title = '${title}', Amount = ${amount}, OtherInfo = '${otherInfo}',
                    Category = '${category}', Method = '${method}', 
                    IsIncome = ${expenseType.includes('INCOME') ? 1: 0}
                    WHERE Id = ${id};` 
            } 
            else {
                sql = `INSERT INTO Expense (Day, Month, Year, Title, Amount, OtherInfo, Category, Method, IsIncome) VALUES 
                (
                    ${date.getDate()}, '${Months[date.getMonth()]}', ${date.getFullYear()},
                    '${title}', ${amount}, '${otherInfo}', '${category}', '${method}',
                    ${expenseType.includes('INCOME') ? 1: 0}
                );`
            }
            executeSql(sql, () => {
                ToastAndroid.show(`Expense ${id > 0 ? 'modified' : 'created'}!`, ToastAndroid.SHORT);
                navigation.navigate('Main');
            });
        }
    }
    const onDeletePressed = () => {
        executeSql(
            `DELETE FROM Expense WHERE Id = ${id};`,
            () => {
            ToastAndroid.show(`Expense deleted!`, ToastAndroid.SHORT);
            navigation.navigate('Main');
        });
    }
    
    return (
        <SafeAreaView style={styles.container}>
            <Text style={styles.title}> {pageTitle} </Text>
            <CustomButton 
                title={expenseType} onPress={onExpsenseTypePressed} style={{marginBottom: 20}}
                bgColor={expenseType.includes('INCOME') ? Colors.primary: Colors.secondary} 
                fgColor={Colors.white} type='secondary'/>
            <CustomInput 
                placeholder='Description' iconName={Icons.Description} 
                color={Colors.secondary} value={title} setValue={setTitle}/>
            <CustomInput 
                placeholder='Amount' iconName={Icons.Amount} color={Colors.secondary}
                value={amount} setValue={setAmount}/>
            <CustomInput 
                placeholder='Other Info' iconName={Icons.OtherInfo} color={Colors.secondary}
                value={otherInfo} setValue={setOtherInfo}/>
            <View style={styles.buttonContainer}>
                <CustomButtonIcon style={{width: '47%'}} iconName={Icons.Category}
                    title={category} onPress={()=>{onOpen('category')}} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
                <CustomButtonIcon style={{width: '47%'}} iconName={Icons.Method}
                    title={method} onPress={()=>{onOpen('method')}} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
                <Picker
                    id='category' label='Select Category' data={categoryData} 
                    searchable={false} renderListItem={renderCategoryItem}
                />
                <Picker
                    id='method' label='Select Payment Method' data={methodData} 
                    searchable={false} renderListItem={renderMethodItem}
                />
            </View>
            <View style={styles.buttonContainer}>
                <CustomButtonIcon style={{width: '47%'}} iconName={Icons.Date}
                    title={date.toDateString()} onPress={showDatepicker} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
                <CustomButtonIcon style={{width: '47%'}} iconName={Icons.Files}
                    title='Add files' onPress={showFilePicker} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
            </View>
            <CustomButton 
                title='Confirm' onPress={onConfirmPressed} style={{marginTop: 50}}
                bgColor={Colors.primary} fgColor={Colors.white} type='primary'/>
            {
                (id >= 0) ? <CustomButtonIcon iconName={Icons.Delete}
                title='' onPress={onDeletePressed}  style={styles.deleteButton}
                bgColor={Colors.secondary} fgColor={Colors.white}/> : null
            }
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        backgroundColor: Colors.background,
        flex: 1,
        paddingTop: Platform.OS === 'android' ? StatusBar.currentHeight : 0,
        alignItems: 'center',
    },
    title: {
        fontSize: Size.font_sl,
        fontWeight: '500',
        color: Colors.secondary,
        marginTop: 40,
        marginBottom: 20,
    },
    buttonContainer: {
        width: '80%',
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    itemContainer: {
        height: 40,
        borderBottomWidth: 1,
        justifyContent: 'center',
    },
    item: {
        fontSize: Size.font_l
    },
    deleteButton: {
        width: 60,  
        height: 60,   
        borderRadius: 30,            
        position: 'absolute',                                          
        paddingHorizontal: 15,
        bottom: 20,                                                    
        right: 20,
    }
})
export default ViewExpenseScreen;