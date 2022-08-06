import { DateTimePickerAndroid } from '@react-native-community/datetimepicker';
import { onOpen, onClose, Picker } from 'react-native-actions-sheet-picker';
import { FontAwesome } from '@expo/vector-icons';

import { useIsFocused, useNavigation } from '@react-navigation/native';
import React, { useEffect, useState } from 'react';
import { View, StyleSheet, Text, Pressable, ToastAndroid, SafeAreaView, StatusBar, Platform } from 'react-native';

import CustomButton from '../components/CustomButton';
import CustomInput from '../components/CustomInput';
import { executeSql, Months } from '../components/Sql';
import Colors from '../config/Colors';
import Size from '../config/Size';
import CustomButtonIcon from '../components/CustomButtonIcon';
import Icons from '../config/Icons';


function TransferMoneyScreen({ route }) {
    const {pageTitle, id} = route.params;

    const [title, setTitle] = useState('');
    const [amount, setAmount] = useState('');
    const [accountList, setAccountList] = useState([]);
    const [accountFrom, setAccountFrom] = useState('From');
    const [accountTo, setAccountTo] = useState('To');
    const [date, setDate] = useState(new Date());
    const navigation = useNavigation();

    const isFocused = useIsFocused();
    useEffect(() => {
        if (isFocused) {
            executeSql(
                'SELECT Name FROM Account;',
                (tx, results) => {
                    var len = results.rows.length;
                    var data = []
                    for (let i=0; i<len; i++) {
                        data.push(results.rows.item(i).Name);
                    }
                    setAccountList(data);
                }
            );
            if (id >= 0) {
                executeSql(
                    `SELECT * FROM Expense WHERE Id = ${id};`,
                    (tx, results) => {
                        var len = results.rows.length;
                        if (len == 1) {
                            var sqlRow = results.rows.item(0);
                            var [from, to] = sqlRow.OtherInfo.split(' --> ');
                            setTitle(sqlRow.Title);
                            setAmount(sqlRow.Amount.toString());
                            setAccountFrom(from);
                            setAccountTo(to);
                            setDate(new Date(sqlRow.Year, Months.indexOf(sqlRow.Month), sqlRow.Day));
                        } else {
                            console.log('Transfer Viewing Error');
                        }
                    }
                );
            }
        }
    }, [isFocused]);

    const onChange = (event, selectedDate) => {
        const currentDate = selectedDate;
        setDate(currentDate);
    };
    const showDatepicker = () => {
        DateTimePickerAndroid.open({
            value: date, onChange, mode: 'date',
        });
    }

    const renderAccountFromItem = (item) => {
        return (
            <Pressable style={styles.itemContainer} onPress={() => {
                setAccountFrom(item);
                onClose('From');
            }}>
                <Text style={styles.item}> {item} </Text>
            </Pressable>
        );
    }
    const renderAccountToItem = (item) => {
        return (
            <Pressable style={styles.itemContainer} onPress={() => {
                setAccountTo(item);
                onClose('To');
            }}>
                <Text style={styles.item}> {item} </Text>
            </Pressable>
        );
    }

    const onConfirmPressed = () => {
        if (amount.trim().length == 0 || isNaN(amount)) {
            Alert.alert('Warning!', `Amount ${amount} invalid!`);
        } else if (accountTo === 'To' || accountFrom === 'From') {
            Alert.alert('Warning!', `Account To/From invalid!`);
        } else if (title.trim().length === 0){
            Alert.alert('Warning!', `Description Empty!`);
        } else {
            let sql;
            if (id > 0) {
                sql = `UPDATE Expense SET Day = ${date.getDate()},
                    Month = '${Months[date.getMonth()]}', Year = ${date.getFullYear()},
                    Title = '${title}', Amount = ${amount}, Category = 'Transfer',
                    OtherInfo = '${accountFrom} --> ${accountTo}'
                    WHERE Id = ${id};` 
            } 
            else {
                sql = `INSERT INTO Expense (Day, Month, Year, Title, Amount, OtherInfo, Category) VALUES 
                (
                    ${date.getDate()}, '${Months[date.getMonth()]}', ${date.getFullYear()},
                    '${title}', ${amount}, '${accountFrom} --> ${accountTo}', 'Transfer'
                );`
            }
            executeSql(sql, () => {
                ToastAndroid.show(`Transfer ${id > 0 ? 'modified' : 'created'}!`, ToastAndroid.SHORT);
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
            <CustomInput 
                placeholder='Description' iconName={Icons.Description} 
                color={Colors.secondary} value={title} setValue={setTitle}/>
            <CustomInput 
                placeholder='Amount' iconName={Icons.Amount} color={Colors.secondary}
                value={amount} setValue={setAmount}/>
            <View style={styles.buttonContainer}>
                <CustomButtonIcon style={{width: '44%'}} iconName={Icons.From}
                    title={accountFrom} onPress={()=>{onOpen('From')}} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
                <FontAwesome style={{paddingTop: 10}} name={Icons.Right} size={24} color ={Colors.secondary} />
                <CustomButtonIcon style={{width: '44%'}} iconName={Icons.To}
                    title={accountTo} onPress={()=>{onOpen('To')}} 
                    bgColor={Colors.background} fgColor={Colors.secondary}/>
                <Picker
                    id='From' label='Select Account (From)' data={accountList} 
                    searchable={false} renderListItem={renderAccountFromItem}
                />
                <Picker
                    id='To' label='Select Account (To)' data={accountList} 
                    searchable={false} renderListItem={renderAccountToItem}
                />
            </View>
            <View style={styles.buttonContainer}>
                <CustomButtonIcon style={{width: '100%'}} iconName={Icons.Date}
                    title={date.toDateString()} onPress={showDatepicker} 
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
        alignItems: 'center',
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
export default TransferMoneyScreen;