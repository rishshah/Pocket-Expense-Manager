import { useIsFocused } from '@react-navigation/native';
import React, { useEffect, useState } from 'react';
import { 
    Alert, FlatList, Platform, SafeAreaView, StatusBar, StyleSheet, Text, 
    ToastAndroid, TouchableOpacity, View 
} from 'react-native';
import DialogButton from 'react-native-dialog/lib/Button';
import DialogContainer from 'react-native-dialog/lib/Container';
import DialogInput from 'react-native-dialog/lib/Input';
import { FontAwesome5 } from '@expo/vector-icons';

import CustomButton from '../components/CustomButton';
import SecondaryNavBar from '../components/SecondaryNavBar';
import { executeSql } from '../components/Sql';
import Colors from '../config/Colors';
import Size from '../config/Size';
import { categoryIcons } from '../config/Icons';


function ListScreen({ route }) {
    const { type, title } = route.params;
    const [mylist, setMyList] = useState([]);
    const [visibleCreate, setVisibleCreate] = useState(false);
    const [newListItem, setNewListItem] = useState('');
    const [rerender, setRerender] = useState(false);
    
    const [infoValue, setInfoValue] = useState('');
    const [infoLabel, setInfoLabel] = useState('');
    const [accountList, setAccountList] = useState([]);

    const getListData = (tx, results) => {
        var len = results.rows.length;
        var data = [];
        for (let i=0; i<len; i++) {
            data.push(results.rows.item(i).Name);
        }
        setMyList(data);
    }
    const getAccountListData = (tx, results) => {
        var len = results.rows.length;
        var data = [];
        for (let i=0; i<len; i++) {
            data.push(results.rows.item(i).Name);
        }
        setAccountList(data);
    }

    const isFocused = useIsFocused();
    useEffect(() => {
        if (isFocused) {
            if (type === 'Account') {
                setInfoLabel('Enter Initial Amount');
            }
            else if (type === 'Method') {
                setInfoLabel('Enter Linked Account');
                executeSql(
                    `SELECT Name FROM Account
                    `, getAccountListData
                )
            }
            executeSql(
                `SELECT Name FROM ${type}`, getListData
            );
        }
    }, [isFocused]);

    const onConfirmPressed = () => {
        if (newListItem === '') {
            Alert.alert('Warning!', `New item cannot be empty string!`);
            return;
        } else if (mylist.includes(newListItem)) {
            Alert.alert('Warning!', `${type} ${newListItem} already exists!`);
            return;
        } else {
            let sql;
            if (type === 'Account') {
                sql = `INSERT INTO ${type} (Name, InitialAmount) VALUES('${newListItem}', ${infoValue});`
                if (infoValue.trim().length == 0 || isNaN(infoValue)) {
                    Alert.alert('Warning!', `Amount ${infoValue} not valid number!`);
                    return;
                }                 
            }
            else if (type === 'Method') {
                sql = `INSERT INTO ${type} (Name, Account) VALUES('${newListItem}', '${infoValue}');`
                if (!accountList.includes(infoValue)) {
                    Alert.alert('Warning!', `Account ${infoValue} not valid!`);
                    return;
                }
            }
            else {
                var iconName = (newListItem in categoryIcons) ? categoryIcons[newListItem] : categoryIcons.Other
                sql = `INSERT INTO ${type} (Name, Icon) VALUES('${newListItem}', '${iconName}');`
            }

            executeSql(sql, () => {
                ToastAndroid.show(`${type} ${newListItem} added!`, ToastAndroid.SHORT);
                mylist.push(newListItem);
                setMyList(mylist);
                setRerender(!rerender);
            });
            
        }
        setVisibleCreate(false);
    }

    const onDeletePressed = (item) => {
        const index = mylist.indexOf(item);
        if (index < 0) {
            Alert.alert('Warning!', `${type} ${newListItem} doesn't exist!`);
        } else {
            Alert.alert(
                'Confirm Delete', 
                `Are you sure to delete ${item} ${type} ?`,
                [
                    {  
                        text: 'Cancel',  
                        onPress: () => ToastAndroid.show(
                            `Deletion canceled!`, ToastAndroid.SHORT
                        ),  
                        style: 'cancel',  
                    },  
                    {
                        text: 'Delete', 
                        onPress: () => {
                            executeSql(
                                `DELETE FROM ${type} WHERE Name='${item}';`, 
                                () => {
                                    ToastAndroid.show(
                                        `${type} ${newListItem} Deleted!`, 
                                        ToastAndroid.SHORT
                                    );
                                    mylist.splice(index, 1);
                                    setMyList(mylist);
                                    setRerender(!rerender);
                                }
                            );
                        },
                        style: 'ok',  
                    },  
                ]
            );
        }
    }

    return (
        <SafeAreaView style={styles.container}>
            <SecondaryNavBar title={title}/>
            <FlatList
                data={mylist}
                extraData={rerender}
                renderItem={({ item }) =>(
                    <TouchableOpacity 
                        style={styles.itemContainer}
                        onLongPress={() => {onDeletePressed(item)}}
                    > 
                        { 
                            (type === 'Category') ?
                            <FontAwesome5 name={categoryIcons[item]} size={30} color={Colors.primary} /> :
                            null
                        }
                        <Text style={styles.item}> {item}</Text>
                    </TouchableOpacity>
                )}
            />
            <DialogContainer 
                visible={visibleCreate} 
                onBackdropPress={() => {setVisibleCreate(false)}}
            >
                <DialogInput label={`Enter New ${type}`} onChangeText={setNewListItem}/>
                { 
                    (type === 'Account' || type === 'Method') ? <DialogInput 
                        label={infoLabel} onChangeText={setInfoValue}
                    /> : null
                }
                <DialogButton label='Back' onPress={() => {setVisibleCreate(false)}} />
                <DialogButton label='Confirm' onPress={onConfirmPressed} />
            </DialogContainer>
            <View style={styles.buttonContainer}>
                <CustomButton 
                    onPress={() => {setVisibleCreate(true)}}
                    title={`Add ${type}`} bgColor={Colors.primary} fgColor={Colors.white}
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
    itemContainer: {
        height: 50,
        borderBottomWidth: 0.5,
        padding: 10,
        flexDirection: 'row',
        alignItems: 'center',
    },
    item: {
        fontSize: Size.font_xl,
        paddingHorizontal: 10
    }
})
export default ListScreen;