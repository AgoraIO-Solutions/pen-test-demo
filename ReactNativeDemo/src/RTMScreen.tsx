import React, {useState} from 'react';
import {
  Button,
  FlatList,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput, TouchableOpacity,
  View,
} from "react-native";

const SaySomethingPlaceholder = 'Say something...';
const SendButtonText = 'Send';

const MessageEntry: React.FC<RTMProps> = ({addToMessageRow}) => {
  const [messageText, setMessageText] = useState('');
  const sendText = () => {
    console.log(`TODO: Send the following to RTM ${messageText}`);
    addToMessageRow(messageText);
    setMessageText('');

  };
  return (
    <View style={messageEntryStyles.container}>
      <TextInput
        value={messageText}
        style={messageEntryStyles.textInput}
        placeholder={SaySomethingPlaceholder}
        onChangeText={setMessageText}
        returnKeyType="send"
      />
      <Button
        style={messageEntryStyles.button}
        onPress={sendText}
        disabled={messageText === '' || messageText === null}
        title={SendButtonText}
      />

    </View>
  );
};
const messageEntryStyles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 13,
  },

  textInput: {
    flex: 6,
  },
});

const MessageDisplay: React.FC<RTMProps> = ({messages}) => {
  return (
    <View style={messageDisplayStyles.container}>
      <FlatList
        data={messages}
        renderItem={MessageRow}
        keyExtractor={item => item.title}
      />
    </View>
  );
};

type MessageRowProps = {
  title: string;
};

const MessageRow: React.FC = ({item}) => {
  return (
    <View style={messageDisplayStyles.row}>
      <Text style={messageDisplayStyles.titleText}>{item.title}</Text>
    </View>
  );
};

const messageDisplayStyles = StyleSheet.create({
  container: {
    flex: 8,
  },
  titleText: {
    fontSize: 34
  },
  row: {
    fontSize: 44,
    fontWeight: "600",
    paddingTop: 34,
    paddingHorizontal: 21,
  },
});

type RTMProps = {
  messages: MessageRowProps[];
  addToMessageRow: (msg: string) => void;
};

const RTMScreen: React.FC = () => {
  const preFab: MessageRowProps[] = ['dog', 'cat', 'bird', 'cow'].map(it => {
    return {title: it};
  });

  const [messages, setMessages] = useState<MessageRowProps[]>(preFab);

  const addMessageRow = (msg: string) => {
    setMessages([{ title: msg }].concat(messages));
  };
  return (
    <SafeAreaView style={styles.container}>
      <MessageEntry messages={messages} addToMessageRow={addMessageRow} />
      <MessageDisplay messages={messages} addToMessageRow={addMessageRow} />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

export default RTMScreen;
